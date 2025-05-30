package com.realive.domain.seller;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSellerDocument is a Querydsl query type for SellerDocument
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSellerDocument extends EntityPathBase<SellerDocument> {

    private static final long serialVersionUID = -129056320L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSellerDocument sellerDocument = new QSellerDocument("sellerDocument");

    public final com.realive.domain.common.QBaseTimeEntity _super = new com.realive.domain.common.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final EnumPath<com.realive.domain.common.enums.SellerFileType> fileType = createEnum("fileType", com.realive.domain.common.enums.SellerFileType.class);

    public final StringPath fileUrl = createString("fileUrl");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isVerified = createBoolean("isVerified");

    public final QSeller seller;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final DateTimePath<java.time.LocalDateTime> verifiedAt = createDateTime("verifiedAt", java.time.LocalDateTime.class);

    public QSellerDocument(String variable) {
        this(SellerDocument.class, forVariable(variable), INITS);
    }

    public QSellerDocument(Path<? extends SellerDocument> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSellerDocument(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSellerDocument(PathMetadata metadata, PathInits inits) {
        this(SellerDocument.class, metadata, inits);
    }

    public QSellerDocument(Class<? extends SellerDocument> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.seller = inits.isInitialized("seller") ? new QSeller(forProperty("seller")) : null;
    }

}

