package com.diegobezerra.core.cinemais.domain.model

data class State(
    val name: String,
    val fu: String
) {
    companion object {

        fun buildFromFU(FU: String): State? {
            val name = getNameFromFU(FU)
            return if (name != null) {
                State(name = name, fu = FU)
            } else {
                null
            }
        }

        private fun getNameFromFU(FU: String): String? {
            return when (FU) {
                "AC" -> "Acre"
                "AL" -> "Alagoas"
                "AM" -> "Amazonas"
                "AP" -> "Amapá"
                "BA" -> "Bahia"
                "CE" -> "Ceará"
                "DF" -> "Distrito Federal"
                "ES" -> "Espírito Santo"
                "GO" -> "Goiânia"
                "MA" -> "Maranhão"
                "MG" -> "Minas Gerais"
                "MS" -> "Mato Grosso do Sul"
                "MT" -> "Mato Grosso"
                "PA" -> "Pará"
                "PB" -> "Paraíba"
                "PE" -> "Pernambuco"
                "PI" -> "Piauí"
                "PR" -> "Paraná"
                "RJ" -> "Rio de Janeiro"
                "RN" -> "Rio Grande do Norte"
                "RO" -> "Rondônia"
                "RR" -> "Roraima"
                "RS" -> "Rio Grade do Sul"
                "SC" -> "Santa Catarina"
                "SE" -> "Sergipe"
                "SP" -> "São Paulo"
                "TO" -> "Tocantins"
                else -> null
            }
        }
    }
}