package com.temaerovendas.domain.model

/**
 * Texto padrão do termo de responsabilidade de inspeção pré-compra.
 * Exibido obrigatoriamente antes de qualquer solicitação de proposta.
 */
object DisclaimerContent {

    const val TITLE = "Termo de Responsabilidade — Inspeção Pré-Compra"

    const val BODY = """A TEM Aerovendas atua exclusivamente como plataforma de divulgação e ` +
        "intermediação de anúncios de aeronaves, conectando potenciais compradores e vendedores.
 
Ao prosseguir com esta solicitação de proposta, o comprador declara estar ciente de que:
 
• As informações técnicas exibidas nesta ficha (horas de voo, ciclos, configuração, ano de fabricação e demais especificações) são fornecidas pelo anunciante e não foram verificadas de forma independente pela TEM Aerovendas.
 
• É de responsabilidade exclusiva do comprador, antes de qualquer compromisso financeiro, contratar uma inspeção pré-compra (pre-purchase inspection) independente, realizada por organização de manutenção certificada (OMA/MRO) de sua escolha.
 
• A TEM Aerovendas não garante, audita ou se responsabiliza pelo estado de manutenção, registros de aeronavegabilidade, histórico de manutenção, danos prévios ou conformidade regulatória da aeronave anunciada.
 
• Qualquer negociação, due diligence documental e contratual deve ser conduzida diretamente entre as partes, recomendando-se o acompanhamento de advogado especializado em transações aeronáuticas.
 
A ausência de uma inspeção pré-compra independente é de inteira responsabilidade do comprador, e a TEM Aerovendas não pode ser responsabilizada por quaisquer divergências entre o anúncio e o estado real da aeronave."""

    const val CHECKBOX_LABEL = "Li e estou de acordo com os termos acima. Entendo que a inspeção pré-compra independente é minha responsabilidade."

    const val CONFIRM_BUTTON = "Confirmar e Solicitar Proposta"
    const val CANCEL_BUTTON = "Cancelar"
}