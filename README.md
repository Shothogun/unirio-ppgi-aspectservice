# unirio-ppgi-aspectservice
YAWL custom service to enable aspect-oriented business process execution on YAWL.

Softwares necessários:

• JavaJDK7ou8
• PostgreSQL 9.x
• YAWL 3.0.1
• DotNet Framework 4.0
• Microsoft Visual Studio 2010 em diante*

Obs1: ambiente testado e validado somente no Windows 7. Outras versões podem ser compatíveis.
Obs2: recomenda-se que tenha pelo menos 4 GB de RAM disponível para poder rodar todo o ambiente.

* Visual Studio é necessário somente para edição do código fonte do projeto Pointcut Editor.

Instalação dos softwares:

• Java JDK - Realize a instalação do Java JDK de acordo com as instruções constante no site da Oracle.
• PostgreSQL - instale de acordo com as instruções do assistente.
• YAWL - instale de acordo com as instruções do assistente. Instale o YAWL em um diretório de fácil acesso pelo prompt de comando (ex. C:\YAWL).
• DotNet Framework - instale de acordo com as instruções do assistente.
• Visual Studio - instale de acordo com as instruções do assistente. Deve-se instalar o suporte ao Windows Forms usando a linguagem C#.

Configuração dos softwares:

PostgreSQL

Durante a instalação do PostgreSQL, defina a senha do usuário postgres para yawl.

Java

Após realizar a instalação, configure a variável de ambiente JAVA_HOME em Control Panel -> System -> Advanced System Settings -> aba Advanced -> Environment Variables. Defina para a variável o caminho de instalação do JDK.

YAWL

Após a instalação os documentos do YAWL já são reconhecidos pelo Windows. Contudo, nenhum atalho é criado para o editor e nem para a engine. Para iniciar o editor, vá no diretório de instalação do YAWL via prompt de comando e execute o arquivo YAWL.bat da seguinte forma: .\YAWL.bat editor.

Para executar a engine, execute o seguinte comando: .\YAWL.bat controlpanel

O painel de controle aberto permite controlar a inicialização e encerramento da engine do YAWL. Para acessar a parte administrativa da engine, abra um navegador e digite o endereço http://localhost:8080/resourceService. Login: admin; senha: YAWL.

Execução dos exemplos:

Crie o diretório C:\Java\aspect e copie os diretórios rules e specs que vieram no arquivo compactado.

Realize a instalação do Pointcut Editor que se encontra na pasta Install, dentro do arquivo compactado.

Vá no pgAdmin e conecte no PostgreSQL local. Crie uma nova base de dados chamada yawl e defina como owner o usuário postgres.

Copie o arquivo aspectService.war para o diretório engine/apache- tomcat-7.0.55/webapps.

Abra o painel de controle do YAWL e inicie a engine.

Iniciado o Tomcat, adicione o AspectService ao ambiente do YAWL. Acesse http://localhost:8080/resourceService com o login admin e senha YAWL. Vá na opção Service e entre com as informações listadas abaixo:

AspectService
Name: aspectService
Password: yAspect
URI: http://localhost:8080/aspectService/ib
Description: A implementation for aspect-oriented business process modeling

Agora vá na opção Users e crie um novo usuário administrador com todas as permissões disponíveis.

Pra finalizar, vá na opção Cases e carregue os exemplos desejados. Faça logout e entre com o usuário criado. Vá em Cases, selecione um processo e inicie um novo caso em Lanch Case.
