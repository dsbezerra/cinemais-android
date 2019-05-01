package com.diegobezerra.core.cinemais.domain.model

data class State(
    val name: String,
    val federativeUnit: String
) {
    companion object {

        fun buildFromFederativeUnit(federativeUnit: String): State? {
            val name = getNameFromFederativeUnit(federativeUnit)
            return if (name != null) {
                State (name = name, federativeUnit = federativeUnit)
            } else {
                null
            }
        }

        private fun getNameFromFederativeUnit(federativeUnit: String): String? {
            return when (federativeUnit) {
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