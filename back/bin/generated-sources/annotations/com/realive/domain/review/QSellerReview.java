package com.realive.domain.review;

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

    private static final long serialVersionUID = -814322172L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSellerReview sellerReview = new QSellerReview("sellerReview");

    public final com.realive.domain.common.QBaseTimeEntity _super = new com.realive.domain.common.QBaseTimeEntity(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.realive.domain.customer.QCustomer customer;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isHidden = createBoolean("isHidden");

    public final com.realive.domain.order.QOrder order;

    public final NumberPath<Double> rating = createNumber("rating", Double.class);

    public final com.realive.domain.seller.QSeller seller;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

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
        this.customer = inits.isInitialized("customer") ? new com.realive.domain.customer.QCustomer(forProperty("customer")) : null;
        this.order = inits.isInitialized("order") ? new com.realive.domain.order.QOrder(forProperty("order"), inits.get("order")) : null;
        this.seller = inits.isInitialized("seller") ? new com.realive.domain.seller.QSeller(forProperty("seller")) : null;
    }

}

