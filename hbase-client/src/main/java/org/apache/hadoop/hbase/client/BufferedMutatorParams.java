begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutorService
import|;
end_import

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
name|TableName
import|;
end_import

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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

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
name|classification
operator|.
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * Parameters for instantiating a {@link BufferedMutator}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|BufferedMutatorParams
implements|implements
name|Cloneable
block|{
specifier|static
specifier|final
name|int
name|UNSET
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
specifier|final
name|TableName
name|tableName
decl_stmt|;
specifier|private
name|long
name|writeBufferSize
init|=
name|UNSET
decl_stmt|;
specifier|private
name|int
name|maxKeyValueSize
init|=
name|UNSET
decl_stmt|;
specifier|private
name|ExecutorService
name|pool
init|=
literal|null
decl_stmt|;
specifier|private
name|String
name|implementationClassName
init|=
literal|null
decl_stmt|;
specifier|private
name|BufferedMutator
operator|.
name|ExceptionListener
name|listener
init|=
operator|new
name|BufferedMutator
operator|.
name|ExceptionListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onException
parameter_list|(
name|RetriesExhaustedWithDetailsException
name|exception
parameter_list|,
name|BufferedMutator
name|bufferedMutator
parameter_list|)
throws|throws
name|RetriesExhaustedWithDetailsException
block|{
throw|throw
name|exception
throw|;
block|}
block|}
decl_stmt|;
specifier|public
name|BufferedMutatorParams
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
block|}
specifier|public
name|TableName
name|getTableName
parameter_list|()
block|{
return|return
name|tableName
return|;
block|}
specifier|public
name|long
name|getWriteBufferSize
parameter_list|()
block|{
return|return
name|writeBufferSize
return|;
block|}
comment|/**    * Override the write buffer size specified by the provided {@link Connection}'s    * {@link org.apache.hadoop.conf.Configuration} instance, via the configuration key    * {@code hbase.client.write.buffer}.    */
specifier|public
name|BufferedMutatorParams
name|writeBufferSize
parameter_list|(
name|long
name|writeBufferSize
parameter_list|)
block|{
name|this
operator|.
name|writeBufferSize
operator|=
name|writeBufferSize
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|int
name|getMaxKeyValueSize
parameter_list|()
block|{
return|return
name|maxKeyValueSize
return|;
block|}
comment|/**    * Override the maximum key-value size specified by the provided {@link Connection}'s    * {@link org.apache.hadoop.conf.Configuration} instance, via the configuration key    * {@code hbase.client.keyvalue.maxsize}.    */
specifier|public
name|BufferedMutatorParams
name|maxKeyValueSize
parameter_list|(
name|int
name|maxKeyValueSize
parameter_list|)
block|{
name|this
operator|.
name|maxKeyValueSize
operator|=
name|maxKeyValueSize
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ExecutorService
name|getPool
parameter_list|()
block|{
return|return
name|pool
return|;
block|}
comment|/**    * Override the default executor pool defined by the {@code hbase.htable.threads.*}    * configuration values.    */
specifier|public
name|BufferedMutatorParams
name|pool
parameter_list|(
name|ExecutorService
name|pool
parameter_list|)
block|{
name|this
operator|.
name|pool
operator|=
name|pool
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * @return Name of the class we will use when we construct a    * {@link BufferedMutator} instance or null if default implementation.    */
specifier|public
name|String
name|getImplementationClassName
parameter_list|()
block|{
return|return
name|this
operator|.
name|implementationClassName
return|;
block|}
comment|/**    * Specify a BufferedMutator implementation other than the default.    * @param implementationClassName Name of the BufferedMutator implementation class    */
specifier|public
name|BufferedMutatorParams
name|implementationClassName
parameter_list|(
name|String
name|implementationClassName
parameter_list|)
block|{
name|this
operator|.
name|implementationClassName
operator|=
name|implementationClassName
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|BufferedMutator
operator|.
name|ExceptionListener
name|getListener
parameter_list|()
block|{
return|return
name|listener
return|;
block|}
comment|/**    * Override the default error handler. Default handler simply rethrows the exception.    */
specifier|public
name|BufferedMutatorParams
name|listener
parameter_list|(
name|BufferedMutator
operator|.
name|ExceptionListener
name|listener
parameter_list|)
block|{
name|this
operator|.
name|listener
operator|=
name|listener
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.lang.Object#clone()    */
specifier|public
name|BufferedMutatorParams
name|clone
parameter_list|()
block|{
name|BufferedMutatorParams
name|clone
init|=
operator|new
name|BufferedMutatorParams
argument_list|(
name|this
operator|.
name|tableName
argument_list|)
decl_stmt|;
name|clone
operator|.
name|writeBufferSize
operator|=
name|this
operator|.
name|writeBufferSize
expr_stmt|;
name|clone
operator|.
name|maxKeyValueSize
operator|=
name|maxKeyValueSize
expr_stmt|;
name|clone
operator|.
name|pool
operator|=
name|this
operator|.
name|pool
expr_stmt|;
name|clone
operator|.
name|listener
operator|=
name|this
operator|.
name|listener
expr_stmt|;
name|clone
operator|.
name|implementationClassName
operator|=
name|this
operator|.
name|implementationClassName
expr_stmt|;
return|return
name|clone
return|;
block|}
block|}
end_class

end_unit

