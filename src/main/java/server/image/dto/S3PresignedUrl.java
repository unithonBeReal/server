package server.image.dto;

public record S3PresignedUrl(String presignedUrl, String filePathInDB) {
}
