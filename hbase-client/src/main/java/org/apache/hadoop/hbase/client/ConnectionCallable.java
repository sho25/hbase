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
name|java
operator|.
name|io
operator|.
name|Closeable
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

begin_comment
comment|/**  * A RetryingCallable for generic connection operations.  * @param<V> return type  */
end_comment

begin_class
specifier|abstract
class|class
name|ConnectionCallable
parameter_list|<
name|V
parameter_list|>
implements|implements
name|RetryingCallable
argument_list|<
name|V
argument_list|>
implements|,
name|Closeable
block|{
specifier|protected
name|Connection
name|connection
decl_stmt|;
specifier|public
name|ConnectionCallable
parameter_list|(
specifier|final
name|Connection
name|connection
parameter_list|)
block|{
name|this
operator|.
name|connection
operator|=
name|connection
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|prepare
parameter_list|(
name|boolean
name|reload
parameter_list|)
throws|throws
name|IOException
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|throwable
parameter_list|(
name|Throwable
name|t
parameter_list|,
name|boolean
name|retrying
parameter_list|)
block|{   }
annotation|@
name|Override
specifier|public
name|String
name|getExceptionMessageAdditionalDetail
parameter_list|()
block|{
return|return
literal|""
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|sleep
parameter_list|(
name|long
name|pause
parameter_list|,
name|int
name|tries
parameter_list|)
block|{
return|return
name|ConnectionUtils
operator|.
name|getPauseTime
argument_list|(
name|pause
argument_list|,
name|tries
argument_list|)
return|;
block|}
block|}
end_class

end_unit

