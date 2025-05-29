package com.realive.domain.product;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDeliveryPolicy is a Querydsl query type for DeliveryPolicy
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDeliveryPolicy extends EntityPathBase<DeliveryPolicy> {

    private static final long serialVersionUID = -831721842L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QDeliveryPolicy deliveryPolicy = new QDeliveryPolicy("deliveryPolicy");

    public final com.realive.domain.common.QBaseTimeEntity _super = new com.realive.domain.common.QBaseTimeEntity(this);

    public final NumberPath<Integer> cost = createNumber("cost", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QProduct product;

    public final StringPath regionLimit = createString("regionLimit");

    public final EnumPath<com.realive.domain.common.enums.DeliveryType> type = createEnum("type", com.realive.domain.common.enums.DeliveryType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QDeliveryPolicy(String variable) {
        this(DeliveryPolicy.class, forVariable(variable), INITS);
    }

    public QDeliveryPolicy(Path<? extends DeliveryPolicy> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QDeliveryPolicy(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QDeliveryPolicy(PathMetadata metadata, PathInits inits) {
        this(DeliveryPolicy.class, metadata, inits);
    }

    public QDeliveryPolicy(Class<? extends DeliveryPolicy> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.product = inits.isInitialized("product") ? new QProduct(forProperty("product"), inits.get("product")) : null;
    }

}

