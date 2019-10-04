<p align="center"><img src="logo/512px.png" alt="Cine+" height="200px"></p>

Cine+
===================================

Este aplicativo serve como uma alternativa para o site da rede de cinemas [Cinemais](http://www.cinemais.com.br).
É possível ver:
* Filmes em cartaz e próximos lançamentos.
* Programação, tabela de preços e outras informações de cada cinema da rede.
* Detalhes de um determinado filme, como sinopse, duração, gêneros, trailers e mais.

Este aplicativo foi construído para por em prática conhecimentos de algumas bibliotecas encontradas no Android Architecture Components, então é possível ver o uso dos seguintes componentes: [ViewModels](https://developer.android.com/reference/android/arch/lifecycle/ViewModel.html), [LiveData](https://developer.android.com/reference/android/arch/lifecycle/LiveData.html) e [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager).

[<img src="https://play.google.com/intl/pt_br/badges/images/generic/pt_badge_web_generic.png"
     alt="Disponível no Google Play"
     height="80">](https://play.google.com/store/apps/details?id=com.diegobezerra.cinemaisapp)

## Screenshots

| Tela Inicial | Filmes | Detalhes Filme | Detalhes Cinema | Modo Escuro |
|:-:|:-:|:-:|:-:|:-:|
| ![First](/.github/assets/main_screen.png?raw=true) | ![Sec](/.github/assets/movies_screen.png?raw=true) | ![Third](/.github/assets/movie_screen.png?raw=true) | ![Fourth](/.github/assets/cinema_screen.png?raw=true) | ![Fifth](/.github/assets/dark_mode.png?raw=true) |

Introdução
------------

### Funcionalidades

Esse aplicativo possui as seguintes telas:
* Uma tela inicial semelhante a tela inicial da versão desktop do site oficial;
* Uma tela com os filmes em cartaz (em todos os cinemas da rede) e próximos lançamentos;
* Detalhes de um filme específico (sinopse, gêneros, elenco, duração, etc);
* Uma lista de cinemas da rede;
* Detalhes de um cinema específico (programação, tabela de preços, localização, etc);
* Uma tela de configurações bem simples com opção de escolher entre modo escuro e padrão;
* Uma tela com informações do aplicativo e informações de contato;
* Filtro de programação e notificações de estreias da semana para o cinema escolhido.

#### Arquitetura

A arquitetura foi criada com base nos componentes encontrados no [Android Architecture Components](https://developer.android.com/topic/libraries/architecture/).

Model-View-ViewModel (MVVM) foi usado para a camada de apresentação. A View e [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)s se comunicam usando [LiveData](https://developer.android.com/topic/libraries/architecture/livedata) e a [Data Binding Library](https://developer.android.com/topic/libraries/data-binding/) foi utilizado "bindar" os dados na interface do usuário.

Repository Pattern foi usado para lidar com as operações de dados. Todos os dados do Cinemais são retirados diretamente do site oficial através do uso das bibliotecas [Retrofit](https://github.com/square/retrofit), para efetuar as requisições HTTP, e [jsoup](https://jsoup.org), para extrair os dados de cada página. Um modo off-line simples foi implementado através da função de cache do [OkHttp](https://github.com/square/okhttp).

#### Outras bibliotecas de terceiros usadas

  * [Dagger 2][1] para dependency injection e reduzir código boilerplate.
  * [Glide][2] para carregar imagens de forma eficiente.
  * [LeakCanary][3] para detectar vazamentos de memória.
  * [Mockito][4] Unit tests.
  * [Notify][5] para criar notificações de forma simples.
  * [Timber][6] logs úteis durante o desenvolvimento.

[1]: https://github.com/google/dagger
[2]: https://github.com/bumptech/glide
[3]: https://github.com/square/leakcanary
[4]: https://site.mockito.org/
[5]: https://github.com/Karn/notify
[6]: https://github.com/JakeWharton/timber


License
-------

Copyright 2019 Diego Bezerra.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.
