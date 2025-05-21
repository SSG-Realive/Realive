package com.realive.domain.seller;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSellerReview is a Querydsl query type for SellerReview
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSellerReview extends EntityPathBase<SellerReview> {

    private static final long serialVersionUID = -1310798531L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSellerReview sellerReview = new QSellerReview("sellerReview");

    public final StringPath content = createString("content");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> rating = createNumber("rating", Integer.class);

    public final QSeller seller;

    public QSellerReview(String variable) {
        this(SellerReview.class, forVariable(variable), INITS);
    }

    public QSellerReview(Path<? extends SellerReview> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSellerReview(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSellerReview(PathMetadata metadata, PathInits inits) {
        this(SellerReview.class, metadata, inits);
    }

    public QSellerReview(Class<? extends SellerReview> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.seller = inits.isInitialized("seller") ? new QSeller(forProperty("seller")) : null;
    }

}

