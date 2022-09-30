package domain.records

case class UserRecord(
    id: Int,
    userName: String,
    pwHash: String
)
