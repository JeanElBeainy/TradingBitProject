# TradingBit

A simulated cryptocurrency trading platform built with Spring Boot that provides real-time market data and trading functionality for educational purposes.

## 🚨 Important Disclaimer

**TradingBit is a simulation platform for educational purposes only.** No real money or cryptocurrency is used, stored, or transacted. All balances, trades, and transactions are purely fictional and for demonstration purposes. This is a university student project to showcase web development skills.

## Features

- **User Authentication**: Secure registration and login system with Spring Security
- **Portfolio Management**: Track your simulated portfolio and trading history
- **Real-time Cryptocurrency Data**: Live market data from CoinMarketCap API
- **Simulated Trading**: Swap between different cryptocurrencies with realistic pricing
- **Real-time Updates**: Auto-updating prices and portfolio values

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- CoinMarketCap API key (free tier available)
- Database (MySQL is suggested)

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/JeanElBeainy/TradingBitProject.git
cd TradingBitProject
```

### 2. Set Up Environment Variables

Inside the `application-dev.yaml` file in `src/main/resources/`, you will need to provide:

- The name of the database
- The username used for the database
- The password used for accessing the database

```properties
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/{DATABASE_NAME_HERE}?createDatabaseIfNotExist=true
    username: {YOUR_USERNAME}
    password: {YOUR_PASSWORD}
  jpa:
    show-sql: true
websiteUrl: http://localhost:8080
server:
  servlet:
    session:
      timeout: 10m
```

Now inside the `pom.xml` file:

```properties
<plugin>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-maven-plugin</artifactId>
    <configuration>
        <url>jdbc:mysql://localhost:3306/{DATABASE_NAME_HERE}?createDatabaseIfNotExist=true</url>
        <user>{YOUR_USERNAME}</user>
        <password>{YOUR_PASSWORD}</password>
        <cleanDisabled>false</cleanDisabled>
    </configuration>
</plugin>
```

Inside the `application-prod.yaml` file in `src/main/resources/`, you will need to provide:

- The spring datasource url, which is used in the dev YAML file: `jdbc:mysql://localhost:3306/{DATABASE_NAME_HERE}?createDatabaseIfNotExist=true`
(NOTE: the prod file is not needed for local usage)

```properties
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
websiteUrl: {WEB_URL / can clear it out since it is not used for local usage}
server:
  servlet:
    session:
      timeout: 10m
```

### 3. Database Setup

Create a database named `tradingbit_db` (or your preferred name) in your database system.
NOTE: if you wish to choose another name, you will have to modify the name in the `application.yaml` file and inside the `pom.xml` file as shown above.


### 4. Get CoinMarketCap API Key

1. Visit [CoinMarketCap API](https://coinmarketcap.com/api/)
2. Sign up for a free account
3. Generate an API key
4. Add it to your `.env` file: `CMC_API_KEY=YOUR_KEY_HERE`


### 5. Build and Run

```bash
# Migrate to your database to set up all the tables
mvn flyway:migrate

# Validate the changes
mvn flyway:validate
```

And run the program.

The application will be available at `http://localhost:8080`

## Usage

### Getting Started
1. Navigate to `http://localhost:8080`
2. Click "Sign Up" to create a new account
3. Fill out the registration form with a unique password
4. Sign in with your credentials

### Trading Simulation
1. Visit the **Dashboard** to view current cryptocurrency prices
2. Go to **Swap** to simulate trading between cryptocurrencies
3. Select cryptocurrencies from your portfolio to trade
4. Enter the amount you want to swap
5. Complete the simulated transaction

### Portfolio Management
- View your **Account Overview** to see portfolio balance and trading history
- Monitor real-time updates of your simulated holdings
- Track all your trading activity and performance

## 🤝 Contributing

This is a personal educational project, but suggestions and feedback are welcome! If you'd like to contribute, you can fork the repository and open a pull request.

## ⚠️ Security Notes

- Use a unique password when testing - don't reuse passwords from other accounts
- This is a demonstration project, not a production-ready financial application

## License

This project is for educational purposes. Please see the disclaimer page for full terms.

## 👨‍💻 Author

**Jean El Beainy**
- GitHub: [@JeanElBeainy](https://github.com/JeanElBeainy)
- LinkedIn: [Jean El Beainy](https://www.linkedin.com/in/jeanelbeainy/)

---

**Remember: This is a simulation platform for educational purposes only. No real financial transactions occur.**
