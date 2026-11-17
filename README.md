# BookStore Enterprise — Aula 29

Solução do exercício de DAO e mapeamento objeto-relacional, em Java 17 e SQLite.

## Estrutura

```text
bookstore-enterprise/
├── pom.xml
├── README.md
├── .gitignore
└── src/
    ├── main/java/br/com/bookstore/
    │   ├── Main.java
    │   ├── model/
    │   │   └── Livro.java
    │   ├── dao/
    │   │   ├── LivroDAO.java
    │   │   └── LivroDAOSqlite.java
    │   └── infra/
    │       └── ConexaoFactory.java
    └── test/java/br/com/bookstore/
        └── BookstoreTest.java
```

## Executar

Requisitos: JDK 17 ou superior e Maven 3.9 ou superior. É necessário acesso à internet na primeira compilação para baixar as dependências.

Abra a pasta que contém `pom.xml` no IntelliJ, Eclipse ou VS Code e importe como projeto Maven. Aguarde a resolução das dependências e execute `br.com.bookstore.Main`.

Também é possível executar pelo terminal, dentro desta pasta:

```shell
mvn clean test
mvn compile exec:java
```

O SQLite cria automaticamente `bookstore.db` no diretório de execução. Não é necessário instalar um servidor de banco de dados.

Na primeira execução com um banco novo, o programa cadastra três livros com IDs 1, 2 e 3, consulta os IDs 2 e 999 e lista o catálogo. Cada nova execução adiciona mais três livros e preserva os existentes; os IDs continuam crescendo. O ID 999 é ausente no cenário inicial, mas poderá existir se o banco receber muitos registros.

Para repetir o cenário inicial, escolha um novo nome de arquivo, ainda inexistente:

```shell
mvn compile exec:java "-Dbookstore.jdbc.url=jdbc:sqlite:outra-demonstracao.db"
```

## Responsabilidades

- `Livro`: atributos, dois construtores, validações, getters, único setter `setId` e apresentação formatada. Também rejeita preços NaN e infinitos.
- `LivroDAO`: contrato com `salvar`, `buscarPorId` e `listarTodos`, sem importar JDBC.
- `LivroDAOSqlite`: criação da tabela no construtor, SQL parametrizado, captura de chave com `Statement.RETURN_GENERATED_KEYS` e mapeamento centralizado. A gravação usa transação para reverter a inserção caso a recuperação da chave falhe.
- `ConexaoFactory`: abertura de conexões e configuração da URL.
- `Main`: aplicação de console com o roteiro solicitado, consumindo a interface DAO. A implementação concreta é escolhida na inicialização.

As conexões, os statements e os resultados são fechados com `try-with-resources` nos métodos que os abrem. O mapeador utiliza o `ResultSet` do chamador e não o fecha prematuramente.

O método `salvar` representa uma inserção, conforme o enunciado; não implementa atualização. `Optional.empty()` representa ausência de registro, enquanto erros de banco são propagados como exceções com a causa original.

## Testes

Os seis testes verificam validações do domínio, construtores, banco vazio, geração dos IDs, recuperação dos atributos, persistência entre instâncias, ordenação da listagem, busca ausente e armazenamento seguro de texto com aspas e acentos. Cada teste de DAO usa um arquivo SQLite temporário, independente do catálogo da aplicação.

Driver: [SQLite JDBC da Xerial](https://github.com/xerial/sqlite-jdbc), com versão fixada no `pom.xml` para reprodução do ambiente.
