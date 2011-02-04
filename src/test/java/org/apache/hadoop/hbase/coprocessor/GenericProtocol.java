begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|coprocessor
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ipc
operator|.
name|CoprocessorProtocol
import|;
end_import

begin_interface
specifier|public
interface|interface
name|GenericProtocol
extends|extends
name|CoprocessorProtocol
block|{
comment|/**    * Simple interface to allow the passing of a generic parameter to see if the    * RPC framework can accommodate generics.    *     * @param<T>    * @param genericObject    * @return    */
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
name|doWork
parameter_list|(
name|T
name|genericObject
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

