package com.realive.domain.order;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrderDelivery is a Querydsl query type for OrderDelivery
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrderDelivery extends EntityPathBase<OrderDelivery> {

    private static final long serialVersionUID = 1418054137L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrderDelivery orderDelivery = new QOrderDelivery("orderDelivery");

    public final com.realive.domain.common.QBaseTimeEntity _super = new com.realive.domain.common.QBaseTimeEntity(this);

    public final StringPath carrier = createString("carrier");

    public final DateTimePath<java.time.LocalDateTime> completeDate = createDateTime("completeDate", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final EnumPath<com.realive.domain.common.enums.DeliveryStatus> deliveryStatus = createEnum("deliveryStatus", com.realive.domain.common.enums.DeliveryStatus.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QOrder order;

    public final DateTimePath<java.time.LocalDateTime> startDate = createDateTime("startDate", java.time.LocalDateTime.class);

    public final StringPath trackingNumber = createString("trackingNumber");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QOrderDelivery(String variable) {
        this(OrderDelivery.class, forVariable(variable), INITS);
    }

    public QOrderDelivery(Path<? extends OrderDelivery> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrderDelivery(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrderDelivery(PathMetadata metadata, PathInits inits) {
        this(OrderDelivery.class, metadata, inits);
    }

    public QOrderDelivery(Class<? extends OrderDelivery> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.order = inits.isInitialized("order") ? new QOrder(forProperty("order"), inits.get("order")) : null;
    }

}

