package server.image;

import server.image.dto.S3PresignedUrl;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AwsS3ImageService {

    private final AmazonS3Client amazonsS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.prefix}")
    private String prefixUrl;

    /**
     * presigned url 발급
     *
     * @param fileName 클라이언트가 전달한 파일명 파라미터
     * @return presigned url
     */
    public S3PresignedUrl getPreSignedUrl(String fileName, ImageCategory imageCategory) {
        String folderName = "images/" + imageCategory.name().toLowerCase();
        String path = createPath(fileName, folderName);
        GeneratePresignedUrlRequest generatePresignedUrlRequest = getGeneratePreSignedUrlRequest(bucket, path);
        URL url = amazonsS3Client.generatePresignedUrl(generatePresignedUrlRequest);

        return new S3PresignedUrl(url.toString(), addPrefixUrl(path));
    }

    /**
     * 파일 업로드용(PUT) presigned url 생성
     *
     * @param bucket 버킷 이름
     * @param path   S3 업로드용 파일 이름
     * @return presigned url
     */
    private GeneratePresignedUrlRequest getGeneratePreSignedUrlRequest(String bucket, String path) {
        return new GeneratePresignedUrlRequest(bucket, path)
                .withMethod(HttpMethod.PUT)
                .withExpiration(getPreSignedUrlExpiration());
    }

    /**
     * presigned url 유효 기간 설정
     *
     * @return 유효기간
     */
    private Date getPreSignedUrlExpiration() {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 2;
        expiration.setTime(expTimeMillis);
        return expiration;
    }

    /**
     * 파일의 전체 경로를 생성
     *
     * @return 파일의 전체 경로
     */
    private String createPath(String fileName, String folderName) {
        String fileId = createFileId();
        return String.format("%s/%s_%s", folderName, fileId, fileName);
    }


    /**
     * 파일 고유 ID를 생성
     *
     * @return 36자리의 UUID
     */
    private String createFileId() {
        return UUID.randomUUID().toString();
    }

    private String addPrefixUrl(String path) {
        return prefixUrl + path;
    }

    /**
     * 파일 직접 삭제
     */
    public void deleteFile(String path) {
        try {
            amazonsS3Client.deleteObject(bucket, path);
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
        }
    }



    public void deleteFileWithPrefix(String fullPath) {
        String path = fullPath;
        if (path.contains(prefixUrl)) {
            path = path.substring(prefixUrl.length());
        }
        deleteFile(path);
    }

    public void deleteFilesWithPrefix(List<String> fullPaths) {
        for (String fullPath : fullPaths) {
            String path = fullPath;
            if (path.contains(prefixUrl)) {
                path = path.substring(prefixUrl.length());
            }
            deleteFile(path);
        }
    }

}
