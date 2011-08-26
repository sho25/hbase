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
name|io
operator|.
name|hfile
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_comment
comment|/**  * Interface for a deserializer. Throws an IOException if the serialized data is  * incomplete or wrong.  * */
end_comment

begin_interface
specifier|public
interface|interface
name|CacheableDeserializer
parameter_list|<
name|T
extends|extends
name|Cacheable
parameter_list|>
block|{
comment|/**    * Returns the deserialized object.    *    * @return T the deserialized object.    */
specifier|public
name|T
name|deserialize
parameter_list|(
name|ByteBuffer
name|b
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

