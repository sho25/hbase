begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

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
name|client
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|RpcCallback
import|;
end_import

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
name|io
operator|.
name|InterruptedIOException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * For implementation coprocessor related methods in {@link Table} and {@link Admin} interface.  * @deprecated since 3.0.0, will be removed in 4.0.0 along with the coprocessor related methods in  *             {@link Table} and {@link Admin} interface.  */
end_comment

begin_class
annotation|@
name|Deprecated
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|CoprocessorBlockingRpcCallback
parameter_list|<
name|R
parameter_list|>
implements|implements
name|RpcCallback
argument_list|<
name|R
argument_list|>
block|{
specifier|private
name|R
name|result
decl_stmt|;
specifier|private
name|boolean
name|resultSet
init|=
literal|false
decl_stmt|;
comment|/**    * Called on completion of the RPC call with the response object, or {@code null} in the case of    * an error.    * @param parameter the response object or {@code null} if an error occurred    */
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|(
name|R
name|parameter
parameter_list|)
block|{
synchronized|synchronized
init|(
name|this
init|)
block|{
name|result
operator|=
name|parameter
expr_stmt|;
name|resultSet
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Returns the parameter passed to {@link #run(Object)} or {@code null} if a null value was    * passed. When used asynchronously, this method will block until the {@link #run(Object)} method    * has been called.    * @return the response object or {@code null} if no response was passed    */
specifier|public
specifier|synchronized
name|R
name|get
parameter_list|()
throws|throws
name|IOException
block|{
while|while
condition|(
operator|!
name|resultSet
condition|)
block|{
try|try
block|{
name|this
operator|.
name|wait
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
name|InterruptedIOException
name|exception
init|=
operator|new
name|InterruptedIOException
argument_list|(
name|ie
operator|.
name|getMessage
argument_list|()
argument_list|)
decl_stmt|;
name|exception
operator|.
name|initCause
argument_list|(
name|ie
argument_list|)
expr_stmt|;
throw|throw
name|exception
throw|;
block|}
block|}
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

