LACO    MACRO   &V
        LDA     &V
VOLTA   SUB     #1
        JGT     VOLTA
        MEND
        START   0
        LACO    N1
        LACO    N2
        END
        