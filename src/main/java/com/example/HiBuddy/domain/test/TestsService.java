package com.example.HiBuddy.domain.test;

import com.example.HiBuddy.domain.script.Scripts;
import com.example.HiBuddy.domain.script.ScriptsRepository;
import com.example.HiBuddy.domain.test.dto.response.TestsResponseDto;
import com.example.HiBuddy.domain.user.Users;
import com.example.HiBuddy.domain.user.UsersRepository;
import com.example.HiBuddy.global.config.ClovaSpeechService;
import com.example.HiBuddy.global.config.PraatService;
import com.example.HiBuddy.global.response.code.resultCode.ErrorStatus;
import com.example.HiBuddy.global.response.exception.handler.ScriptsHandler;
import com.example.HiBuddy.global.response.exception.handler.TestsHandler;
import com.example.HiBuddy.global.response.exception.handler.UsersHandler;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestsService {
    private final UsersRepository usersRepository;
    private final ScriptsRepository scriptsRepository;
    private final TestsRepository testsRepository;
    private final ClovaSpeechService clovaSpeechService;
    private final PraatService praatService;
    private final LevenshteinDistance levenshteinDistance = new LevenshteinDistance();

    @Transactional
    public TestsResponseDto.TestResultDto performTest(Long userId, Long scriptId, MultipartFile audioFile) {
        try {
            Users user = usersRepository.findById(userId)
                    .orElseThrow(() -> new UsersHandler(ErrorStatus.USER_NOT_FOUND));

            Scripts script = scriptsRepository.findById(scriptId)
                    .orElseThrow(() -> new ScriptsHandler(ErrorStatus.SCRIPT_NOT_FOUND));

            String recognizedText = clovaSpeechService.recognizeSpeech(convertMultipartFileToBytes(audioFile));
            int pronunciationScore = calculatePronunciationScore(script.getText(), recognizedText);
            String audioFilePath = saveAudioFile(audioFile);
            int meanPitch = generateRandomPitch();
            double basePitch = 110.0;
            String pitchLevel = determinePitchLevel(meanPitch, basePitch);
            LocalDateTime testDate = LocalDateTime.now();

            Test test = new Test();
            test.setUser(user);
            test.setScript(script);
            test.setScriptName(script.getScriptName());
            test.setTestDate(testDate);
            test.setDifficulty(script.getDifficulty().toString());
            test.setRecognizedText(recognizedText);
            test.setPitch(meanPitch);
            test.setBasePitch(basePitch);
            test.setPitchLevel(pitchLevel);
            test.setPronunciationScore(pronunciationScore);

            Test savedTest = testsRepository.save(test);
            return convertToResponseDto(savedTest);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public TestsResponseDto.TestHistoryMainPageListDto getTestHistory(Long userId, int page, int size) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new UsersHandler(ErrorStatus.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size);
        Page<Test> testsPage = testsRepository.findByUser(user, pageable);

        List<TestsResponseDto.TestDto> tests = testsPage.getContent().stream()
                .map(this::convertToTestDto)
                .collect(Collectors.toList());

        return TestsResponseDto.TestHistoryMainPageListDto.builder()
                .test(tests)
                .totalPages(testsPage.getTotalPages())
                .totalElements((int) testsPage.getTotalElements())
                .isFirst(testsPage.isFirst())
                .isLast(testsPage.isLast())
                .number(testsPage.getNumber() + 1)
                .numberOfElements(testsPage.getNumberOfElements())
                .build();
    }

    @Transactional(readOnly = true)
    public TestsResponseDto.TestHistoryDto getTestById(Long testId, Long userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new UsersHandler(ErrorStatus.USER_NOT_FOUND));

        Test test = testsRepository.findByIdAndUser(testId, user)
                .orElseThrow(() -> new TestsHandler(ErrorStatus.TEST_NOT_FOUND));

        return convertToHistoryDto(test);
    }

    private TestsResponseDto.TestHistoryDto convertToHistoryDto(Test test) {
        return TestsResponseDto.TestHistoryDto.builder()
                .testId(test.getId())
                .scriptId(test.getScript().getId())
                .scriptName(test.getScriptName())
                .testDate(test.getTestDate().toString())
                .difficulty(test.getDifficulty())
                .recognizedText(test.getRecognizedText())
                .pitch(test.getPitch())
                .basePitch(test.getBasePitch())
                .pitchLevel(test.getPitchLevel())
                .pronunciationScore(test.getPronunciationScore())
                .build();
    }

    private byte[] convertMultipartFileToBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to convert MultipartFile to byte array", e);
        }
    }

    private int calculatePronunciationScore(String scriptText, String recognizedText) {
        int distance = levenshteinDistance.apply(scriptText, recognizedText);
        int maxLength = Math.max(scriptText.length(), recognizedText.length());
        return (int) ((1 - (double) distance / maxLength) * 100);
    }

    private String saveAudioFile(MultipartFile audioFile) {
        try {
            String fileName = UUID.randomUUID().toString() + ".wav";
            String filePath = Paths.get(System.getProperty("java.io.tmpdir"), fileName).toString();
            File file = new File(filePath);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(audioFile.getBytes());
            }
            return filePath;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save audio file", e);
        }
    }

    private String determinePitchLevel(double meanPitch, double basePitch) {
        if (meanPitch < 134) {
            return "low";
        } else if (meanPitch <= 187) {
            return "medium";
        } else {
            return "high";
        }
    }

    private TestsResponseDto.TestResultDto convertToResponseDto(Test test) {
        TestsResponseDto.UserResponseDto userDto = TestsResponseDto.UserResponseDto.builder()
                .userId(test.getUser().getId())
                .username(test.getUser().getUsername())
                .nickname(test.getUser().getNickname())
                .build();

        return TestsResponseDto.TestResultDto.builder()
                .testId(test.getId())
                .scriptId(test.getScript().getId())
                .scriptName(test.getScriptName())
                .testDate(test.getTestDate().toString())
                .difficulty(test.getDifficulty())
                .recognizedText(test.getRecognizedText())
                .pitch(test.getPitch())
                .basePitch(test.getBasePitch())
                .pitchLevel(test.getPitchLevel())
                .pronunciationScore(test.getPronunciationScore())
                .user(userDto)
                .build();
    }

    private TestsResponseDto.TestDto convertToTestDto(Test test) {
        return TestsResponseDto.TestDto.builder()
                .testId(test.getId())
                .scriptId(test.getScript().getId())
                .scriptName(test.getScriptName())
                .testDate(test.getTestDate().toString())
                .build();
    }

    private int generateRandomPitch() {
        Random random = new Random();
        return 60 + random.nextInt(130);
    }
}

