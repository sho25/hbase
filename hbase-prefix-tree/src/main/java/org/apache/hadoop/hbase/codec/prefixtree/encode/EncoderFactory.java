begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|codec
operator|.
name|prefixtree
operator|.
name|encode
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
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
comment|/**  * Retrieve PrefixTreeEncoders from this factory which handles pooling them and preparing the  * ones retrieved from the pool for usage.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|EncoderFactory
block|{
specifier|private
specifier|static
specifier|final
name|EncoderPool
name|POOL
init|=
operator|new
name|EncoderPoolImpl
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|PrefixTreeEncoder
name|checkOut
parameter_list|(
name|OutputStream
name|outputStream
parameter_list|,
name|boolean
name|includeMvccVersion
parameter_list|)
block|{
return|return
name|POOL
operator|.
name|checkOut
argument_list|(
name|outputStream
argument_list|,
name|includeMvccVersion
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|void
name|checkIn
parameter_list|(
name|PrefixTreeEncoder
name|encoder
parameter_list|)
block|{
name|POOL
operator|.
name|checkIn
argument_list|(
name|encoder
argument_list|)
expr_stmt|;
block|}
comment|/**************************** helper ******************************/
specifier|protected
specifier|static
name|PrefixTreeEncoder
name|prepareEncoder
parameter_list|(
name|PrefixTreeEncoder
name|encoder
parameter_list|,
name|OutputStream
name|outputStream
parameter_list|,
name|boolean
name|includeMvccVersion
parameter_list|)
block|{
name|PrefixTreeEncoder
name|ret
init|=
name|encoder
decl_stmt|;
if|if
condition|(
name|encoder
operator|==
literal|null
condition|)
block|{
name|ret
operator|=
operator|new
name|PrefixTreeEncoder
argument_list|(
name|outputStream
argument_list|,
name|includeMvccVersion
argument_list|)
expr_stmt|;
block|}
name|ret
operator|.
name|reset
argument_list|(
name|outputStream
argument_list|,
name|includeMvccVersion
argument_list|)
expr_stmt|;
return|return
name|ret
return|;
block|}
block|}
end_class

end_unit

