DROP DATABASE IF EXISTS Synkork;
CREATE DATABASE Synkork;
USE Synkork;

create table users
(
    id           binary(16)                            not null,
    username     varchar(50)                           not null,
    display_Name varchar(100)                          null,
    email        varchar(100)                          not null,
    password     varchar(255)                          not null,
    avatar_url   varchar(255)                          null,
    avatar_id    varchar(100)                          null,
    provider     varchar(50)                           null,
    role         varchar(30) default 'USER'            not null,
    status       varchar(30) default 'ACTIVE'          not null,
    created_at   datetime    default CURRENT_TIMESTAMP null,
    updated_at   datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    primary key (id),
    constraint email
        unique (email),
    constraint username
        unique (username)
);

create table edit_history
(
    id            binary(16)                         not null,
    resource_type varchar(30)                        not null,
    resource_id   binary(16)                         not null,
    action        varchar(30)                        not null,
    edited_by     binary(16)                         not null,
    edited_at     datetime default CURRENT_TIMESTAMP null,
    primary key (id),
    constraint fk_history_user
        foreign key (edited_by) references users (id)
);

create table friend_requests
(
    id          binary(16)                            not null,
    sender_id   binary(16)                            not null,
    receiver_id binary(16)                            not null,
    status      varchar(20) default 'PENDING'         not null,
    message     text                                  null,
    created_at  datetime    default CURRENT_TIMESTAMP null,
    updated_at  datetime                              null,
    primary key (id),
    constraint uk_friend_request
        unique (sender_id, receiver_id),
    constraint fk_fr_receiver
        foreign key (receiver_id) references users (id),
    constraint fk_fr_sender
        foreign key (sender_id) references users (id)
);

create index idx_fr_receiver
    on friend_requests (receiver_id);

create index idx_fr_sender
    on friend_requests (sender_id);

create table friends
(
    id         binary(16)                         not null,
    user_id    binary(16)                         not null,
    friend_id  binary(16)                         not null,
    created_at datetime default CURRENT_TIMESTAMP null,
    primary key (id),
    constraint uk_user_friend
        unique (user_id, friend_id),
    constraint fk_friend_friend
        foreign key (friend_id) references users (id),
    constraint fk_friend_user
        foreign key (user_id) references users (id)
);

create index idx_friend_friend
    on friends (friend_id);

create index idx_friend_user
    on friends (user_id);

create table rooms
(
    id          binary(16)                            not null,
    name        varchar(100)                          not null,
    room_avatar varchar(255)                          null,
    type        varchar(20) default 'GROUP'           not null,
    owner_id    binary(16)                            not null,
    created_at  datetime    default CURRENT_TIMESTAMP null,
    updated_at  datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    primary key (id),
    constraint fk_room_owner
        foreign key (owner_id) references users (id)
);

create table room_members
(
    id        binary(16)                            not null,
    room_id   binary(16)                            not null,
    user_id   binary(16)                            not null,
    role      varchar(30) default 'MEMBER'          not null,
    joined_at datetime    default CURRENT_TIMESTAMP null,
    primary key (id),
    constraint uk_room_user
        unique (room_id, user_id),
    constraint fk_rm_room
        foreign key (room_id) references rooms (id),
    constraint fk_rm_user
        foreign key (user_id) references users (id)
);

create index idx_rm_room
    on room_members (room_id);

create index idx_rm_user
    on room_members (user_id);

create table spaces
(
    id         binary(16)                            not null,
    room_id    binary(16)                            not null,
    type       varchar(30) default 'CHAT'            not null,
    name       varchar(100)                          not null,
    created_at datetime    default CURRENT_TIMESTAMP null,
    updated_at datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    primary key (id),
    constraint fk_space_room
        foreign key (room_id) references rooms (id)
);

create table boards
(
    id       binary(16)   not null,
    space_id binary(16)   not null,
    name     varchar(100) not null,
    primary key (id),
    constraint fk_board_space
        foreign key (space_id) references spaces (id)
);

create table calendar_events
(
    id             binary(16)                           not null,
    space_id       binary(16)                           not null,
    title          varchar(200)                         not null,
    description    text                                 null,
    event_date     date                                 not null,
    start_time     time                                 not null,
    end_time       time                                 not null,
    allow_edit_all tinyint(1) default 0                 null,
    created_by     binary(16)                           not null,
    created_at     datetime   default CURRENT_TIMESTAMP null,
    updated_at     datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    primary key (id),
    constraint fk_cal_space
        foreign key (space_id) references spaces (id),
    constraint fk_cal_user
        foreign key (created_by) references users (id)
);

create table columns
(
    id       binary(16)   not null,
    board_id binary(16)   not null,
    name     varchar(100) not null,
    position int          not null,
    primary key (id),
    constraint fk_col_board
        foreign key (board_id) references boards (id)
);

create table cards
(
    id          binary(16)                         not null,
    column_id   binary(16)                         not null,
    title       varchar(200)                       not null,
    description text                               null,
    assignee_id binary(16)                         null,
    position    int                                not null,
    created_by  binary(16)                         not null,
    created_at  datetime default CURRENT_TIMESTAMP null,
    updated_at  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    primary key (id),
    constraint fk_card_assignee
        foreign key (assignee_id) references users (id),
    constraint fk_card_column
        foreign key (column_id) references columns (id),
    constraint fk_card_creator
        foreign key (created_by) references users (id)
);

create table messages
(
    id             binary(16)                            not null,
    space_id       binary(16)                            not null,
    sender_id      binary(16)                            not null,
    content        text                                  null,
    type           varchar(20) default 'TEXT'            not null,
    attachment_url varchar(255)                          null,
    pinned         tinyint(1)  default 0                 null,
    deleted        tinyint(1)  default 0                 null,
    created_at     datetime    default CURRENT_TIMESTAMP null,
    updated_at     datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    primary key (id),
    constraint fk_msg_sender
        foreign key (sender_id) references users (id),
    constraint fk_msg_space
        foreign key (space_id) references spaces (id)
);

create table notes
(
    id             binary(16)                           not null,
    space_id       binary(16)                           not null,
    title          text                                 null,
    note           longtext                             null,
    important      tinyint(1) default 0                 null,
    allow_edit_all tinyint(1) default 0                 null,
    created_by     binary(16)                           not null,
    created_at     datetime   default CURRENT_TIMESTAMP null,
    updated_at     datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    primary key (id),
    constraint fk_note_space
        foreign key (space_id) references spaces (id),
    constraint fk_note_user
        foreign key (created_by) references users (id)
);

create index idx_space_room
    on spaces (room_id);


# IMPORT DATA


# User
INSERT INTO synkork.users (id, username, display_Name, email, password, avatar_url, avatar_id, provider, role, status, created_at, updated_at) VALUES (0x712D9ACFF76D11F09E2340C2BA49E1BC, 'admin', 'Nguyễn Cường', 'hieudd2090@gmail.com', 'admin', null, null, null, 'ADMIN', 'ACTIVE', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.users (id, username, display_Name, email, password, avatar_url, avatar_id, provider, role, status, created_at, updated_at) VALUES (0x7132478EF76D11F09E2340C2BA49E1BC, 'lan_anh', 'Trần Lan Anh', 'hieuforcode@gmail.com', '123', null, null, null, 'USER', 'ACTIVE', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.users (id, username, display_Name, email, password, avatar_url, avatar_id, provider, role, status, created_at, updated_at) VALUES (0x7136DD96F76D11F09E2340C2BA49E1BC, 'minh_quan', 'Lê Minh Quân', 'quan@example.com', 'hash_123456', null, null, null, 'USER', 'ACTIVE', '2026-01-22 15:36:27', '2026-01-22 15:36:27');

# Rooms
INSERT INTO synkork.rooms (id, name, room_avatar, type, owner_id, created_at, updated_at) VALUES (0x715A83DEF76D11F09E2340C2BA49E1BC, 'Phòng 1 - Cường', null, 'GROUP', 0x712D9ACFF76D11F09E2340C2BA49E1BC, '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.rooms (id, name, room_avatar, type, owner_id, created_at, updated_at) VALUES (0x715C7488F76D11F09E2340C2BA49E1BC, 'Phòng 2 - Cường', null, 'GROUP', 0x712D9ACFF76D11F09E2340C2BA49E1BC, '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.rooms (id, name, room_avatar, type, owner_id, created_at, updated_at) VALUES (0x715E61DCF76D11F09E2340C2BA49E1BC, 'Phòng 3 - Cường', null, 'GROUP', 0x712D9ACFF76D11F09E2340C2BA49E1BC, '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.rooms (id, name, room_avatar, type, owner_id, created_at, updated_at) VALUES (0x716006B1F76D11F09E2340C2BA49E1BC, 'Phòng 4 - Cường', null, 'GROUP', 0x712D9ACFF76D11F09E2340C2BA49E1BC, '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.rooms (id, name, room_avatar, type, owner_id, created_at, updated_at) VALUES (0x7161ECCDF76D11F09E2340C2BA49E1BC, 'Phòng 5 - Cường', null, 'GROUP', 0x712D9ACFF76D11F09E2340C2BA49E1BC, '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.rooms (id, name, room_avatar, type, owner_id, created_at, updated_at) VALUES (0x7163BD24F76D11F09E2340C2BA49E1BC, 'Project 1 - Lan Anh', null, 'GROUP', 0x7132478EF76D11F09E2340C2BA49E1BC, '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.rooms (id, name, room_avatar, type, owner_id, created_at, updated_at) VALUES (0x71657E39F76D11F09E2340C2BA49E1BC, 'Project 2 - Lan Anh', null, 'GROUP', 0x7132478EF76D11F09E2340C2BA49E1BC, '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.rooms (id, name, room_avatar, type, owner_id, created_at, updated_at) VALUES (0x7167276FF76D11F09E2340C2BA49E1BC, 'Project 3 - Lan Anh', null, 'GROUP', 0x7132478EF76D11F09E2340C2BA49E1BC, '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.rooms (id, name, room_avatar, type, owner_id, created_at, updated_at) VALUES (0x7168F853F76D11F09E2340C2BA49E1BC, 'Project 4 - Lan Anh', null, 'GROUP', 0x7132478EF76D11F09E2340C2BA49E1BC, '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.rooms (id, name, room_avatar, type, owner_id, created_at, updated_at) VALUES (0x716B0BD3F76D11F09E2340C2BA49E1BC, 'Project 5 - Lan Anh', null, 'GROUP', 0x7132478EF76D11F09E2340C2BA49E1BC, '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.rooms (id, name, room_avatar, type, owner_id, created_at, updated_at) VALUES (0x716D3E71F76D11F09E2340C2BA49E1BC, 'Club 1 - Minh Quân', null, 'GROUP', 0x7136DD96F76D11F09E2340C2BA49E1BC, '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.rooms (id, name, room_avatar, type, owner_id, created_at, updated_at) VALUES (0x716FAA3DF76D11F09E2340C2BA49E1BC, 'Club 2 - Minh Quân', null, 'GROUP', 0x7136DD96F76D11F09E2340C2BA49E1BC, '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.rooms (id, name, room_avatar, type, owner_id, created_at, updated_at) VALUES (0x7171C2BFF76D11F09E2340C2BA49E1BC, 'Club 3 - Minh Quân', null, 'GROUP', 0x7136DD96F76D11F09E2340C2BA49E1BC, '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.rooms (id, name, room_avatar, type, owner_id, created_at, updated_at) VALUES (0x7173B93DF76D11F09E2340C2BA49E1BC, 'Club 4 - Minh Quân', null, 'GROUP', 0x7136DD96F76D11F09E2340C2BA49E1BC, '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.rooms (id, name, room_avatar, type, owner_id, created_at, updated_at) VALUES (0x71759258F76D11F09E2340C2BA49E1BC, 'Club 5 - Minh Quân', null, 'GROUP', 0x7136DD96F76D11F09E2340C2BA49E1BC, '2026-01-22 15:36:27', '2026-01-22 15:36:27');

# Space
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x715B9035F76D11F09E2340C2BA49E1BC, 0x715A83DEF76D11F09E2340C2BA49E1BC, 'CHAT', 'Thảo luận', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x715B97F5F76D11F09E2340C2BA49E1BC, 0x715A83DEF76D11F09E2340C2BA49E1BC, 'CHAT', 'Thông báo', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x715B9A20F76D11F09E2340C2BA49E1BC, 0x715A83DEF76D11F09E2340C2BA49E1BC, 'CHAT', 'Góc tâm sự', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x715C075CF76D11F09E2340C2BA49E1BC, 0x715A83DEF76D11F09E2340C2BA49E1BC, 'VOICE', 'Phòng học nhóm', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x715C0DF4F76D11F09E2340C2BA49E1BC, 0x715A83DEF76D11F09E2340C2BA49E1BC, 'VOICE', 'Kênh nghe nhạc', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x715D75E8F76D11F09E2340C2BA49E1BC, 0x715C7488F76D11F09E2340C2BA49E1BC, 'CHAT', 'Thảo luận', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x715D7D57F76D11F09E2340C2BA49E1BC, 0x715C7488F76D11F09E2340C2BA49E1BC, 'CHAT', 'Thông báo', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x715D7E96F76D11F09E2340C2BA49E1BC, 0x715C7488F76D11F09E2340C2BA49E1BC, 'CHAT', 'Góc tâm sự', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x715DEBBDF76D11F09E2340C2BA49E1BC, 0x715C7488F76D11F09E2340C2BA49E1BC, 'VOICE', 'Phòng học nhóm', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x715DF06CF76D11F09E2340C2BA49E1BC, 0x715C7488F76D11F09E2340C2BA49E1BC, 'VOICE', 'Kênh nghe nhạc', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x715F36C6F76D11F09E2340C2BA49E1BC, 0x715E61DCF76D11F09E2340C2BA49E1BC, 'CHAT', 'Thảo luận', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x715F3A70F76D11F09E2340C2BA49E1BC, 0x715E61DCF76D11F09E2340C2BA49E1BC, 'CHAT', 'Thông báo', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x715F3B1BF76D11F09E2340C2BA49E1BC, 0x715E61DCF76D11F09E2340C2BA49E1BC, 'CHAT', 'Góc tâm sự', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x715F9D47F76D11F09E2340C2BA49E1BC, 0x715E61DCF76D11F09E2340C2BA49E1BC, 'VOICE', 'Phòng học nhóm', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x715FA1D2F76D11F09E2340C2BA49E1BC, 0x715E61DCF76D11F09E2340C2BA49E1BC, 'VOICE', 'Kênh nghe nhạc', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x716102E9F76D11F09E2340C2BA49E1BC, 0x716006B1F76D11F09E2340C2BA49E1BC, 'CHAT', 'Thảo luận', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x71610B01F76D11F09E2340C2BA49E1BC, 0x716006B1F76D11F09E2340C2BA49E1BC, 'CHAT', 'Thông báo', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x71610C86F76D11F09E2340C2BA49E1BC, 0x716006B1F76D11F09E2340C2BA49E1BC, 'CHAT', 'Góc tâm sự', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x7161793AF76D11F09E2340C2BA49E1BC, 0x716006B1F76D11F09E2340C2BA49E1BC, 'VOICE', 'Phòng học nhóm', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x71617EEBF76D11F09E2340C2BA49E1BC, 0x716006B1F76D11F09E2340C2BA49E1BC, 'VOICE', 'Kênh nghe nhạc', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x7162D45FF76D11F09E2340C2BA49E1BC, 0x7161ECCDF76D11F09E2340C2BA49E1BC, 'CHAT', 'Thảo luận', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x7162DC03F76D11F09E2340C2BA49E1BC, 0x7161ECCDF76D11F09E2340C2BA49E1BC, 'CHAT', 'Thông báo', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x7162DD45F76D11F09E2340C2BA49E1BC, 0x7161ECCDF76D11F09E2340C2BA49E1BC, 'CHAT', 'Góc tâm sự', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x71634BA7F76D11F09E2340C2BA49E1BC, 0x7161ECCDF76D11F09E2340C2BA49E1BC, 'VOICE', 'Phòng học nhóm', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x71635401F76D11F09E2340C2BA49E1BC, 0x7161ECCDF76D11F09E2340C2BA49E1BC, 'VOICE', 'Kênh nghe nhạc', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x71649CB2F76D11F09E2340C2BA49E1BC, 0x7163BD24F76D11F09E2340C2BA49E1BC, 'CHAT', 'General', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x7164A0AAF76D11F09E2340C2BA49E1BC, 0x7163BD24F76D11F09E2340C2BA49E1BC, 'CHAT', 'Tasks', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x7164A148F76D11F09E2340C2BA49E1BC, 0x7163BD24F76D11F09E2340C2BA49E1BC, 'CHAT', 'Chém gió', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x716504E3F76D11F09E2340C2BA49E1BC, 0x7163BD24F76D11F09E2340C2BA49E1BC, 'VOICE', 'Họp khẩn cấp', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x71650AD4F76D11F09E2340C2BA49E1BC, 0x7163BD24F76D11F09E2340C2BA49E1BC, 'VOICE', 'Phòng trà', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x71664D58F76D11F09E2340C2BA49E1BC, 0x71657E39F76D11F09E2340C2BA49E1BC, 'CHAT', 'General', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x71665155F76D11F09E2340C2BA49E1BC, 0x71657E39F76D11F09E2340C2BA49E1BC, 'CHAT', 'Tasks', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x7166521EF76D11F09E2340C2BA49E1BC, 0x71657E39F76D11F09E2340C2BA49E1BC, 'CHAT', 'Chém gió', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x7166B65AF76D11F09E2340C2BA49E1BC, 0x71657E39F76D11F09E2340C2BA49E1BC, 'VOICE', 'Họp khẩn cấp', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x7166BC7EF76D11F09E2340C2BA49E1BC, 0x71657E39F76D11F09E2340C2BA49E1BC, 'VOICE', 'Phòng trà', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x71680BBFF76D11F09E2340C2BA49E1BC, 0x7167276FF76D11F09E2340C2BA49E1BC, 'CHAT', 'General', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x7168114AF76D11F09E2340C2BA49E1BC, 0x7167276FF76D11F09E2340C2BA49E1BC, 'CHAT', 'Tasks', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x7168120AF76D11F09E2340C2BA49E1BC, 0x7167276FF76D11F09E2340C2BA49E1BC, 'CHAT', 'Chém gió', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x716885A6F76D11F09E2340C2BA49E1BC, 0x7167276FF76D11F09E2340C2BA49E1BC, 'VOICE', 'Họp khẩn cấp', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x71688C39F76D11F09E2340C2BA49E1BC, 0x7167276FF76D11F09E2340C2BA49E1BC, 'VOICE', 'Phòng trà', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x716A005CF76D11F09E2340C2BA49E1BC, 0x7168F853F76D11F09E2340C2BA49E1BC, 'CHAT', 'General', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x716A0A38F76D11F09E2340C2BA49E1BC, 0x7168F853F76D11F09E2340C2BA49E1BC, 'CHAT', 'Tasks', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x716A0C14F76D11F09E2340C2BA49E1BC, 0x7168F853F76D11F09E2340C2BA49E1BC, 'CHAT', 'Chém gió', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x716A97BEF76D11F09E2340C2BA49E1BC, 0x7168F853F76D11F09E2340C2BA49E1BC, 'VOICE', 'Họp khẩn cấp', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x716AA088F76D11F09E2340C2BA49E1BC, 0x7168F853F76D11F09E2340C2BA49E1BC, 'VOICE', 'Phòng trà', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x716BFE5DF76D11F09E2340C2BA49E1BC, 0x716B0BD3F76D11F09E2340C2BA49E1BC, 'CHAT', 'General', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x716C0513F76D11F09E2340C2BA49E1BC, 0x716B0BD3F76D11F09E2340C2BA49E1BC, 'CHAT', 'Tasks', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x716C0660F76D11F09E2340C2BA49E1BC, 0x716B0BD3F76D11F09E2340C2BA49E1BC, 'CHAT', 'Chém gió', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x716CB181F76D11F09E2340C2BA49E1BC, 0x716B0BD3F76D11F09E2340C2BA49E1BC, 'VOICE', 'Họp khẩn cấp', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x716CB86CF76D11F09E2340C2BA49E1BC, 0x716B0BD3F76D11F09E2340C2BA49E1BC, 'VOICE', 'Phòng trà', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x716E4E50F76D11F09E2340C2BA49E1BC, 0x716D3E71F76D11F09E2340C2BA49E1BC, 'CHAT', 'Sự kiện', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x716E5641F76D11F09E2340C2BA49E1BC, 0x716D3E71F76D11F09E2340C2BA49E1BC, 'CHAT', 'Waiting room', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x716E5812F76D11F09E2340C2BA49E1BC, 0x716D3E71F76D11F09E2340C2BA49E1BC, 'CHAT', 'Archive', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x716EE65BF76D11F09E2340C2BA49E1BC, 0x716D3E71F76D11F09E2340C2BA49E1BC, 'VOICE', 'Voice Chat 01', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x716EF0B5F76D11F09E2340C2BA49E1BC, 0x716D3E71F76D11F09E2340C2BA49E1BC, 'VOICE', 'Voice Chat 02', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x7170BA54F76D11F09E2340C2BA49E1BC, 0x716FAA3DF76D11F09E2340C2BA49E1BC, 'CHAT', 'Sự kiện', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x7170C3A8F76D11F09E2340C2BA49E1BC, 0x716FAA3DF76D11F09E2340C2BA49E1BC, 'CHAT', 'Waiting room', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x7170C4F5F76D11F09E2340C2BA49E1BC, 0x716FAA3DF76D11F09E2340C2BA49E1BC, 'CHAT', 'Archive', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x717140AFF76D11F09E2340C2BA49E1BC, 0x716FAA3DF76D11F09E2340C2BA49E1BC, 'VOICE', 'Voice Chat 01', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x717147D3F76D11F09E2340C2BA49E1BC, 0x716FAA3DF76D11F09E2340C2BA49E1BC, 'VOICE', 'Voice Chat 02', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x7172C849F76D11F09E2340C2BA49E1BC, 0x7171C2BFF76D11F09E2340C2BA49E1BC, 'CHAT', 'Sự kiện', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x7172D029F76D11F09E2340C2BA49E1BC, 0x7171C2BFF76D11F09E2340C2BA49E1BC, 'CHAT', 'Waiting room', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x7172D128F76D11F09E2340C2BA49E1BC, 0x7171C2BFF76D11F09E2340C2BA49E1BC, 'CHAT', 'Archive', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x71733FA8F76D11F09E2340C2BA49E1BC, 0x7171C2BFF76D11F09E2340C2BA49E1BC, 'VOICE', 'Voice Chat 01', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x7173470AF76D11F09E2340C2BA49E1BC, 0x7171C2BFF76D11F09E2340C2BA49E1BC, 'VOICE', 'Voice Chat 02', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x7174A919F76D11F09E2340C2BA49E1BC, 0x7173B93DF76D11F09E2340C2BA49E1BC, 'CHAT', 'Sự kiện', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x7174AE92F76D11F09E2340C2BA49E1BC, 0x7173B93DF76D11F09E2340C2BA49E1BC, 'CHAT', 'Waiting room', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x7174AF62F76D11F09E2340C2BA49E1BC, 0x7173B93DF76D11F09E2340C2BA49E1BC, 'CHAT', 'Archive', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x7175227FF76D11F09E2340C2BA49E1BC, 0x7173B93DF76D11F09E2340C2BA49E1BC, 'VOICE', 'Voice Chat 01', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x717528FEF76D11F09E2340C2BA49E1BC, 0x7173B93DF76D11F09E2340C2BA49E1BC, 'VOICE', 'Voice Chat 02', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x71769917F76D11F09E2340C2BA49E1BC, 0x71759258F76D11F09E2340C2BA49E1BC, 'CHAT', 'Sự kiện', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x71769F0CF76D11F09E2340C2BA49E1BC, 0x71759258F76D11F09E2340C2BA49E1BC, 'CHAT', 'Waiting room', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x71769FF4F76D11F09E2340C2BA49E1BC, 0x71759258F76D11F09E2340C2BA49E1BC, 'CHAT', 'Archive', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x71771135F76D11F09E2340C2BA49E1BC, 0x71759258F76D11F09E2340C2BA49E1BC, 'VOICE', 'Voice Chat 01', '2026-01-22 15:36:27', '2026-01-22 15:36:27');
INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) VALUES (0x7177190CF76D11F09E2340C2BA49E1BC, 0x71759258F76D11F09E2340C2BA49E1BC, 'VOICE', 'Voice Chat 02', '2026-01-22 15:36:27', '2026-01-22 15:36:27');

# Room member
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x715AEA38F76D11F09E2340C2BA49E1BC, 0x715A83DEF76D11F09E2340C2BA49E1BC, 0x712D9ACFF76D11F09E2340C2BA49E1BC, 'ADMIN', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x715AF26CF76D11F09E2340C2BA49E1BC, 0x715A83DEF76D11F09E2340C2BA49E1BC, 0x7132478EF76D11F09E2340C2BA49E1BC, 'MEMBER', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x715AF5AAF76D11F09E2340C2BA49E1BC, 0x715A83DEF76D11F09E2340C2BA49E1BC, 0x7136DD96F76D11F09E2340C2BA49E1BC, 'MEMBER', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x715CF97DF76D11F09E2340C2BA49E1BC, 0x715C7488F76D11F09E2340C2BA49E1BC, 0x712D9ACFF76D11F09E2340C2BA49E1BC, 'ADMIN', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x715D0292F76D11F09E2340C2BA49E1BC, 0x715C7488F76D11F09E2340C2BA49E1BC, 0x7132478EF76D11F09E2340C2BA49E1BC, 'MEMBER', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x715D052DF76D11F09E2340C2BA49E1BC, 0x715C7488F76D11F09E2340C2BA49E1BC, 0x7136DD96F76D11F09E2340C2BA49E1BC, 'MEMBER', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x715EC0AAF76D11F09E2340C2BA49E1BC, 0x715E61DCF76D11F09E2340C2BA49E1BC, 0x712D9ACFF76D11F09E2340C2BA49E1BC, 'ADMIN', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x715EC795F76D11F09E2340C2BA49E1BC, 0x715E61DCF76D11F09E2340C2BA49E1BC, 0x7132478EF76D11F09E2340C2BA49E1BC, 'MEMBER', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x715EC8EFF76D11F09E2340C2BA49E1BC, 0x715E61DCF76D11F09E2340C2BA49E1BC, 0x7136DD96F76D11F09E2340C2BA49E1BC, 'MEMBER', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x71607F2AF76D11F09E2340C2BA49E1BC, 0x716006B1F76D11F09E2340C2BA49E1BC, 0x712D9ACFF76D11F09E2340C2BA49E1BC, 'ADMIN', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x71608B1FF76D11F09E2340C2BA49E1BC, 0x716006B1F76D11F09E2340C2BA49E1BC, 0x7132478EF76D11F09E2340C2BA49E1BC, 'MEMBER', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x71608DE8F76D11F09E2340C2BA49E1BC, 0x716006B1F76D11F09E2340C2BA49E1BC, 0x7136DD96F76D11F09E2340C2BA49E1BC, 'MEMBER', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x71626080F76D11F09E2340C2BA49E1BC, 0x7161ECCDF76D11F09E2340C2BA49E1BC, 0x712D9ACFF76D11F09E2340C2BA49E1BC, 'ADMIN', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x71626731F76D11F09E2340C2BA49E1BC, 0x7161ECCDF76D11F09E2340C2BA49E1BC, 0x7132478EF76D11F09E2340C2BA49E1BC, 'MEMBER', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x71626882F76D11F09E2340C2BA49E1BC, 0x7161ECCDF76D11F09E2340C2BA49E1BC, 0x7136DD96F76D11F09E2340C2BA49E1BC, 'MEMBER', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x7164370AF76D11F09E2340C2BA49E1BC, 0x7163BD24F76D11F09E2340C2BA49E1BC, 0x7132478EF76D11F09E2340C2BA49E1BC, 'ADMIN', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x71643CE1F76D11F09E2340C2BA49E1BC, 0x7163BD24F76D11F09E2340C2BA49E1BC, 0x712D9ACFF76D11F09E2340C2BA49E1BC, 'MEMBER', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x71643DC2F76D11F09E2340C2BA49E1BC, 0x7163BD24F76D11F09E2340C2BA49E1BC, 0x7136DD96F76D11F09E2340C2BA49E1BC, 'MEMBER', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x7165E1AFF76D11F09E2340C2BA49E1BC, 0x71657E39F76D11F09E2340C2BA49E1BC, 0x7132478EF76D11F09E2340C2BA49E1BC, 'ADMIN', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x7165E76CF76D11F09E2340C2BA49E1BC, 0x71657E39F76D11F09E2340C2BA49E1BC, 0x712D9ACFF76D11F09E2340C2BA49E1BC, 'MEMBER', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x7165E8F6F76D11F09E2340C2BA49E1BC, 0x71657E39F76D11F09E2340C2BA49E1BC, 0x7136DD96F76D11F09E2340C2BA49E1BC, 'MEMBER', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x716793F3F76D11F09E2340C2BA49E1BC, 0x7167276FF76D11F09E2340C2BA49E1BC, 0x7132478EF76D11F09E2340C2BA49E1BC, 'ADMIN', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x71679BC6F76D11F09E2340C2BA49E1BC, 0x7167276FF76D11F09E2340C2BA49E1BC, 0x712D9ACFF76D11F09E2340C2BA49E1BC, 'MEMBER', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x71679D7CF76D11F09E2340C2BA49E1BC, 0x7167276FF76D11F09E2340C2BA49E1BC, 0x7136DD96F76D11F09E2340C2BA49E1BC, 'MEMBER', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x7169739DF76D11F09E2340C2BA49E1BC, 0x7168F853F76D11F09E2340C2BA49E1BC, 0x7132478EF76D11F09E2340C2BA49E1BC, 'ADMIN', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x71697D16F76D11F09E2340C2BA49E1BC, 0x7168F853F76D11F09E2340C2BA49E1BC, 0x712D9ACFF76D11F09E2340C2BA49E1BC, 'MEMBER', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x71697F7FF76D11F09E2340C2BA49E1BC, 0x7168F853F76D11F09E2340C2BA49E1BC, 0x7136DD96F76D11F09E2340C2BA49E1BC, 'MEMBER', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x716B7483F76D11F09E2340C2BA49E1BC, 0x716B0BD3F76D11F09E2340C2BA49E1BC, 0x7132478EF76D11F09E2340C2BA49E1BC, 'ADMIN', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x716B7E43F76D11F09E2340C2BA49E1BC, 0x716B0BD3F76D11F09E2340C2BA49E1BC, 0x712D9ACFF76D11F09E2340C2BA49E1BC, 'MEMBER', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x716B7F98F76D11F09E2340C2BA49E1BC, 0x716B0BD3F76D11F09E2340C2BA49E1BC, 0x7136DD96F76D11F09E2340C2BA49E1BC, 'MEMBER', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x716DCE86F76D11F09E2340C2BA49E1BC, 0x716D3E71F76D11F09E2340C2BA49E1BC, 0x7136DD96F76D11F09E2340C2BA49E1BC, 'ADMIN', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x716DD4E6F76D11F09E2340C2BA49E1BC, 0x716D3E71F76D11F09E2340C2BA49E1BC, 0x712D9ACFF76D11F09E2340C2BA49E1BC, 'MEMBER', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x716DD622F76D11F09E2340C2BA49E1BC, 0x716D3E71F76D11F09E2340C2BA49E1BC, 0x7132478EF76D11F09E2340C2BA49E1BC, 'MEMBER', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x71703197F76D11F09E2340C2BA49E1BC, 0x716FAA3DF76D11F09E2340C2BA49E1BC, 0x7136DD96F76D11F09E2340C2BA49E1BC, 'ADMIN', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x71703AFBF76D11F09E2340C2BA49E1BC, 0x716FAA3DF76D11F09E2340C2BA49E1BC, 0x712D9ACFF76D11F09E2340C2BA49E1BC, 'MEMBER', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x71703CDEF76D11F09E2340C2BA49E1BC, 0x716FAA3DF76D11F09E2340C2BA49E1BC, 0x7132478EF76D11F09E2340C2BA49E1BC, 'MEMBER', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x7172385CF76D11F09E2340C2BA49E1BC, 0x7171C2BFF76D11F09E2340C2BA49E1BC, 0x7136DD96F76D11F09E2340C2BA49E1BC, 'ADMIN', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x71723D9DF76D11F09E2340C2BA49E1BC, 0x7171C2BFF76D11F09E2340C2BA49E1BC, 0x712D9ACFF76D11F09E2340C2BA49E1BC, 'MEMBER', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x71723EA0F76D11F09E2340C2BA49E1BC, 0x7171C2BFF76D11F09E2340C2BA49E1BC, 0x7132478EF76D11F09E2340C2BA49E1BC, 'MEMBER', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x71743700F76D11F09E2340C2BA49E1BC, 0x7173B93DF76D11F09E2340C2BA49E1BC, 0x7136DD96F76D11F09E2340C2BA49E1BC, 'ADMIN', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x71743E32F76D11F09E2340C2BA49E1BC, 0x7173B93DF76D11F09E2340C2BA49E1BC, 0x712D9ACFF76D11F09E2340C2BA49E1BC, 'MEMBER', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x71743F87F76D11F09E2340C2BA49E1BC, 0x7173B93DF76D11F09E2340C2BA49E1BC, 0x7132478EF76D11F09E2340C2BA49E1BC, 'MEMBER', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x7176190BF76D11F09E2340C2BA49E1BC, 0x71759258F76D11F09E2340C2BA49E1BC, 0x7136DD96F76D11F09E2340C2BA49E1BC, 'ADMIN', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x7176237FF76D11F09E2340C2BA49E1BC, 0x71759258F76D11F09E2340C2BA49E1BC, 0x712D9ACFF76D11F09E2340C2BA49E1BC, 'MEMBER', '2026-01-22 15:36:27');
INSERT INTO synkork.room_members (id, room_id, user_id, role, joined_at) VALUES (0x717625A3F76D11F09E2340C2BA49E1BC, 0x71759258F76D11F09E2340C2BA49E1BC, 0x7132478EF76D11F09E2340C2BA49E1BC, 'MEMBER', '2026-01-22 15:36:27');

INSERT INTO synkork.spaces (id, room_id, type, name, created_at, updated_at) 
VALUES (0x7177BFC9F76D11F09E2340C2BA49E1BC, 0x715A83DEF76D11F09E2340C2BA49E1BC, 'CALENDAR', 'Lịch làm việc chung', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

