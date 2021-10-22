package co.hmrc.elis.sandbox.camelspringboot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@XmlRootElement(name = "User")
@XmlAccessorType(XmlAccessType.FIELD)
public class User implements Serializable {
    private static final long serialVersionUID = -8834481196928472797L;

    @XmlElement(name = "id")
    private Integer id;
    @XmlElement(name = "name")
    private String name;
}

