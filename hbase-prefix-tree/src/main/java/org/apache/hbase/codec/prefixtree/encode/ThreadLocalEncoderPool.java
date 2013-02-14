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
name|hadoop
operator|.
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Pool to enable reusing the Encoder objects which can consist of thousands of smaller objects and  * would be more garbage than the data in the block.  A new encoder is needed for each block in  * a flush, compaction, RPC response, etc.  *  * It is not a pool in the traditional sense, but implements the semantics of a traditional pool  * via ThreadLocals to avoid sharing between threads.  Sharing between threads would not be  * very expensive given that it's accessed per-block, but this is just as easy.  *  * This pool implementation assumes there is a one-to-one mapping between a single thread and a  * single flush or compaction.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ThreadLocalEncoderPool
implements|implements
name|EncoderPool
block|{
specifier|private
specifier|static
specifier|final
name|ThreadLocal
argument_list|<
name|PrefixTreeEncoder
argument_list|>
name|ENCODER
init|=
operator|new
name|ThreadLocal
argument_list|<
name|PrefixTreeEncoder
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * Get the encoder attached to the current ThreadLocal, or create a new one and attach it to the    * current thread.    */
annotation|@
name|Override
specifier|public
name|PrefixTreeEncoder
name|checkOut
parameter_list|(
name|OutputStream
name|os
parameter_list|,
name|boolean
name|includeMvccVersion
parameter_list|)
block|{
name|PrefixTreeEncoder
name|builder
init|=
name|ENCODER
operator|.
name|get
argument_list|()
decl_stmt|;
name|builder
operator|=
name|EncoderFactory
operator|.
name|prepareEncoder
argument_list|(
name|builder
argument_list|,
name|os
argument_list|,
name|includeMvccVersion
argument_list|)
expr_stmt|;
name|ENCODER
operator|.
name|set
argument_list|(
name|builder
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|checkIn
parameter_list|(
name|PrefixTreeEncoder
name|encoder
parameter_list|)
block|{
comment|// attached to thread on checkOut, so shouldn't need to do anything here
comment|// do we need to worry about detaching encoders from compaction threads or are the same threads
comment|// used over and over
block|}
block|}
end_class

end_unit

