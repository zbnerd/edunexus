-- 결제 정보를 저장하는 테이블
CREATE TABLE payments
(
    payment_id     INT AUTO_INCREMENT PRIMARY KEY,
    user_id        INT                                 NOT NULL,
    payment_type   VARCHAR(50)                         NOT NULL, -- 결제 유형 구분
    amount         DECIMAL(10, 2)                      NOT NULL,
    payment_method VARCHAR(50)                         NOT NULL,
    payment_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 강의 등록 정보를 저장하는 테이블
CREATE TABLE enrollments
(
    enrollment_id     INT AUTO_INCREMENT PRIMARY KEY,
    user_id           INT       NOT NULL,
    course_id         INT       NOT NULL,
    payment_id        INT       NOT NULL,
    registration_date TIMESTAMP NOT NULL,
    FOREIGN KEY (payment_id) REFERENCES payments (payment_id)
);

-- Indexes for performance optimization
CREATE INDEX idx_enrollments_user_id ON enrollments(user_id);
CREATE INDEX idx_enrollments_course_id ON enrollments(course_id);
CREATE UNIQUE INDEX idx_enrollments_user_course ON enrollments(user_id, course_id);

-- 사용자의 구독 정보를 저장하는 테이블
CREATE TABLE subscriptions
(
    subscription_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT       NOT NULL,
    payment_id      INT       NOT NULL,
    start_date      TIMESTAMP NOT NULL,
    end_date        TIMESTAMP NOT NULL,
    FOREIGN KEY (payment_id) REFERENCES payments (payment_id)
);

-- Composite index for subscription queries by user with ordering by end_date
CREATE INDEX idx_subscriptions_user_end_date ON subscriptions(user_id, end_date DESC);