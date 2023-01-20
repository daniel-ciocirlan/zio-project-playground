package repositories.reviews

import zio.mock.{Mock, mockable}

@mockable[ReviewRepository]
object MockReviewRepository extends Mock[ReviewRepository]
