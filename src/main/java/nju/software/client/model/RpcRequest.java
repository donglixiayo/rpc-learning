package nju.software.client.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Han
 * @Description
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RpcRequest {
    private String interfaceName;
    private String methodName;
}
