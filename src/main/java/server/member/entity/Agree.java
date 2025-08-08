package server.member.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Agree{

    Y("동의"),
    N("거부");

    private String status;
}
