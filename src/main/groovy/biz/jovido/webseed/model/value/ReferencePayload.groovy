package biz.jovido.webseed.model.value

import biz.jovido.webseed.model.FragmentHistory

import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.PrimaryKeyJoinColumn
import javax.persistence.SecondaryTable

/**
 *
 * @author Stephan Grundner
 */
@SecondaryTable(name = 'fragment_payload',
        pkJoinColumns = @PrimaryKeyJoinColumn(name = 'payload_id'))
@Entity
class ReferencePayload extends Payload<FragmentHistory> {

    @ManyToOne
    @JoinColumn(name = 'fragment_history_id', table = 'fragment_payload')
    FragmentHistory value
}
