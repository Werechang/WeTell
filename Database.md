## Database
An overview of the tables with their respective attributes
(Image)

| Table | Attributes | description |
|:------|:-----------|:------------|
| users | id, name, hashedPassword, salt, profile_pic | passwords are hashed in the application with salt and pepper |
| chats | id, profile_pic, name | name and profile_pic only for group chats |
| contacts | user_id, chat_id | the connection between users and chats |
| messages | id, sender_id, chat_id, sent_at, msg_content | msg_content is text content |
