package repositories.companies

import zio.mock.{Mock, mockable}

@mockable[CompanyRepository]
object MockCompanyRepository extends Mock[CompanyRepository]
