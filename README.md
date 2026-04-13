[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/AR7CADm8)
[![Open in Codespaces](https://classroom.github.com/assets/launch-codespace-2972f46106e565e64193e422d61a12cf1da4916b45550586e14ef0a7c637dd04.svg)](https://classroom.github.com/open-in-codespaces?assignment_repo_id=23453435)

# :checkered_flag: Labirinto do saber

API de suporte a sessões terapêuticas e educacionais voltada para profissionais de saúde e educação (como fonoaudiólogos e professores) que trabalham com crianças no desenvolvimento da literacia e comunicação. A plataforma permite criar tarefas pedagógicas, organizá-las em cadernos, conduzir sessões interativas com estudantes e gerar relatórios de desempenho.

## :technologist: Membros da equipe

### Membro 1: 
Nome/Matrícula: Matheus dos Santos Rodrigues - 563640 
Curso: Engenharia de Software

### Membro 2: 
Nome/Matrícula: Natan de Sousa Lucena - 563641 
Curso: Engenharia de Software

## :bulb: Objetivo Geral

Oferecer uma plataforma digital que auxilie educadores e profissionais da saúde (como fonoaudiólogos) no acompanhamento individualizado do desenvolvimento da linguagem e da literacia de crianças, por meio de atividades estruturadas, sessões gamificadas e análise quantitativa de desempenho.

## :eyes: Público-Alvo

- **Profissionais:** Fonoaudiólogos, pedagogos, professores de reforço e terapeutas educacionais que conduzem atendimentos individuais ou em grupo com crianças.
- **Pacientes/Alunos:** Crianças em processo de aquisição da linguagem, com dificuldades de leitura, escrita, vocabulário ou compreensão.

## :star2: Impacto Esperado

- **Para os profissionais:** Redução do tempo gasto na criação e organização de atividades, acesso a métricas objetivas de desempenho por sessão e por categoria (leitura, escrita, vocabulário, compreensão), e geração automatizada de relatórios em PDF para documentar a evolução dos pacientes.
- **Para os alunos/pacientes:** Experiência de aprendizado mais estruturada, com atividades multimídia (imagens e áudios) que tornam o processo mais engajante e acessível.
- **Impacto geral:** Digitalização e profissionalização do acompanhamento terapêutico-educacional, contribuindo para decisões clínicas mais embasadas em dados.

## :triangular_flag_on_post: Principais funcionalidades da aplicação

1. **Cadastro e autenticação de educadores** — Registro, login com JWT, recuperação e atualização de perfil com foto (AWS S3).
2. **Gestão de alunos** — Cadastro de crianças com dados pessoais e vínculo com educadores.
3. **Criação de tarefas pedagógicas** — Atividades de múltipla escolha (com ou sem mídia) categorizadas em Leitura, Escrita, Vocabulário e Compreensão.
4. **Organização em grupos e cadernos** — Agrupamento de tarefas em coleções reutilizáveis (TaskGroup) e cadernos temáticos (TaskNotebook).
5. **Condução de sessões interativas** — Início, resposta e encerramento de sessões com registro de tempo de resposta e resultado por tarefa.
6. **Análise de desempenho** — Cálculo de acurácia por categoria ao longo das sessões do aluno.
7. **Upload de mídia** — Suporte a imagens e áudios nas tarefas via AWS S3.

---

> [!WARNING]
> Daqui em diante o README.md só deve ser preenchido no momento da entrega final.

## Tecnologias:

Liste aqui as tecnologias e bibliotecas que foram utilizadas no projeto.

---

## Instruções para Execução

[Inclua instruções claras sobre como rodar o projeto localmente. Isso é crucial para que você possa testá-lo nas próximas entregas. **Somente caso haja alguma coisa diferente do usual**

```bash
# Clone o repositório
git clone [https://docs.github.com/pt/repositories/creating-and-managing-repositories/about-repositories](https://docs.github.com/pt/repositories/creating-and-managing-repositories/about-repositories)

# Navegue para o diretório
cd [nome-do-repositorio]

# Siga as instruções específicas para a sua tecnologia...
```
