package com.realive.domain.review;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSellerReviewImage is a Querydsl query type for SellerReviewImage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSellerReviewImage extends EntityPathBase<SellerReviewImage> {

    private static final long serialVersionUID = 711552439L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSellerReviewImage sellerReviewImage = new QSellerReviewImage("sellerReviewImage");

    public final com.realive.domain.common.QBaseTimeEntity _super = new com.realive.domain.common.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final QSellerReview review;

    public final BooleanPath thumbnail = createBoolean("thumbnail");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QSellerReviewImage(String variable) {
        this(SellerReviewImage.class, forVariable(variable), INITS);
    }

    public QSellerReviewImage(Path<? extends SellerReviewImage> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSellerReviewImage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSellerReviewImage(PathMetadata metadata, PathInits inits) {
        this(SellerReviewImage.class, metadata, inits);
    }

    public QSellerReviewImage(Class<? extends SellerReviewImage> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.review = inits.isInitialized("review") ? new QSellerReview(forProperty("review"), inits.get("review")) : null;
    }

}

