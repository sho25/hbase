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
name|decode
package|;
end_package

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Queue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|LinkedBlockingQueue
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

begin_comment
comment|/**  * Pools PrefixTreeArraySearcher objects. Each Searcher can consist of hundreds or thousands of  * objects and 1 is needed for each HFile during a Get operation. With tens of thousands of  * Gets/second, reusing these searchers may save a lot of young gen collections.  *<p/>  * Alternative implementation would be a ByteBufferSearcherPool (not implemented yet).  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ArraySearcherPool
block|{
comment|/**    * One decoder is needed for each storefile for each Get operation so we may need hundreds at the    * same time, however, decoding is a CPU bound activity so should limit this to something in the    * realm of maximum reasonable active threads.    */
specifier|private
specifier|static
specifier|final
name|Integer
name|MAX_POOL_SIZE
init|=
literal|1000
decl_stmt|;
specifier|protected
name|Queue
argument_list|<
name|PrefixTreeArraySearcher
argument_list|>
name|pool
init|=
operator|new
name|LinkedBlockingQueue
argument_list|<
name|PrefixTreeArraySearcher
argument_list|>
argument_list|(
name|MAX_POOL_SIZE
argument_list|)
decl_stmt|;
specifier|public
name|PrefixTreeArraySearcher
name|checkOut
parameter_list|(
name|ByteBuffer
name|buffer
parameter_list|,
name|boolean
name|includesMvccVersion
parameter_list|)
block|{
name|PrefixTreeArraySearcher
name|searcher
init|=
name|pool
operator|.
name|poll
argument_list|()
decl_stmt|;
comment|//will return null if pool is empty
name|searcher
operator|=
name|DecoderFactory
operator|.
name|ensureArraySearcherValid
argument_list|(
name|buffer
argument_list|,
name|searcher
argument_list|,
name|includesMvccVersion
argument_list|)
expr_stmt|;
return|return
name|searcher
return|;
block|}
specifier|public
name|void
name|checkIn
parameter_list|(
name|PrefixTreeArraySearcher
name|searcher
parameter_list|)
block|{
name|searcher
operator|.
name|releaseBlockReference
argument_list|()
expr_stmt|;
name|pool
operator|.
name|offer
argument_list|(
name|searcher
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
operator|(
literal|"poolSize:"
operator|+
name|pool
operator|.
name|size
argument_list|()
operator|)
return|;
block|}
block|}
end_class

end_unit

