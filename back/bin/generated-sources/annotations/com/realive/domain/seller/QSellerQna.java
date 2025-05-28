package com.realive.domain.seller;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSellerQna is a Querydsl query type for SellerQna
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSellerQna extends EntityPathBase<SellerQna> {

    private static final long serialVersionUID = -1102368225L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSellerQna sellerQna = new QSellerQna("sellerQna");

    public final com.realive.domain.common.QBaseTimeEntity _super = new com.realive.domain.common.QBaseTimeEntity(this);

    public final StringPath answer = createString("answer");

    public final DateTimePath<java.time.LocalDateTime> answeredAt = createDateTime("answeredAt", java.time.LocalDateTime.class);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final BooleanPath deleted = createBoolean("deleted");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isActive = createBoolean("isActive");

    public final BooleanPath isAnswered = createBoolean("isAnswered");

    public final QSeller seller;

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QSellerQna(String variable) {
        this(SellerQna.class, forVariable(variable), INITS);
    }

    public QSellerQna(Path<? extends SellerQna> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSellerQna(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSellerQna(PathMetadata metadata, PathInits inits) {
        this(SellerQna.class, metadata, inits);
    }

    public QSellerQna(Class<? extends SellerQna> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.seller = inits.isInitialized("seller") ? new QSeller(forProperty("seller")) : null;
    }

}

