package server.music.repository;

import static book.book.music.entity.QMusic.music;
import static book.book.music.entity.QMusicTag.musicTag;
import static book.book.music.entity.QTag.tag;

import server.music.dto.MusicResponse;
import server.music.dto.MusicTagProjection;
import book.book.music.dto.QMusicResponse;
import book.book.music.dto.QMusicTagProjection;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MusicRepositoryImpl implements MusicRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MusicResponse> findAllMusics() {
        return queryFactory
                .select(new QMusicResponse(
                        music.id,
                        music.title,
                        music.durationInSeconds,
                        music.coverImageUrl,
                        music.musicUrl,
                        music.createdDate
                ))
                .from(music)
                .orderBy(music.id.desc())
                .fetch();
    }

    @Override
    public List<MusicTagProjection> findMusicTagsByMusicIds(List<Long> musicIds) {
        return queryFactory
                .select(new QMusicTagProjection(
                        music.id,
                        tag.id,
                        tag.name
                ))
                .from(musicTag)
                .join(musicTag.music, music)
                .join(musicTag.tag, tag)
                .where(music.id.in(musicIds))
                .fetch();
    }
} 
