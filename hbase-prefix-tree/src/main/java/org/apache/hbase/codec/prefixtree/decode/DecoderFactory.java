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

begin_import
import|import
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
name|PrefixTreeBlockMeta
import|;
end_import

begin_import
import|import
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
name|scanner
operator|.
name|CellSearcher
import|;
end_import

begin_comment
comment|/**  * Static wrapper class for the ArraySearcherPool.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|DecoderFactory
block|{
specifier|private
specifier|static
specifier|final
name|ArraySearcherPool
name|POOL
init|=
operator|new
name|ArraySearcherPool
argument_list|()
decl_stmt|;
comment|//TODO will need a PrefixTreeSearcher on top of CellSearcher
specifier|public
specifier|static
name|PrefixTreeArraySearcher
name|checkOut
parameter_list|(
specifier|final
name|ByteBuffer
name|buffer
parameter_list|,
name|boolean
name|includeMvccVersion
parameter_list|)
block|{
if|if
condition|(
name|buffer
operator|.
name|isDirect
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"DirectByteBuffers not supported yet"
argument_list|)
throw|;
comment|// TODO implement PtByteBufferBlockScanner
block|}
name|PrefixTreeArraySearcher
name|searcher
init|=
name|POOL
operator|.
name|checkOut
argument_list|(
name|buffer
argument_list|,
name|includeMvccVersion
argument_list|)
decl_stmt|;
return|return
name|searcher
return|;
block|}
specifier|public
specifier|static
name|void
name|checkIn
parameter_list|(
name|CellSearcher
name|pSearcher
parameter_list|)
block|{
if|if
condition|(
name|pSearcher
operator|==
literal|null
condition|)
block|{
return|return;
block|}
if|if
condition|(
operator|!
operator|(
name|pSearcher
operator|instanceof
name|PrefixTreeArraySearcher
operator|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot return "
operator|+
name|pSearcher
operator|.
name|getClass
argument_list|()
operator|+
literal|" to "
operator|+
name|DecoderFactory
operator|.
name|class
argument_list|)
throw|;
block|}
name|PrefixTreeArraySearcher
name|searcher
init|=
operator|(
name|PrefixTreeArraySearcher
operator|)
name|pSearcher
decl_stmt|;
name|POOL
operator|.
name|checkIn
argument_list|(
name|searcher
argument_list|)
expr_stmt|;
block|}
comment|/**************************** helper ******************************/
specifier|public
specifier|static
name|PrefixTreeArraySearcher
name|ensureArraySearcherValid
parameter_list|(
name|ByteBuffer
name|buffer
parameter_list|,
name|PrefixTreeArraySearcher
name|searcher
parameter_list|,
name|boolean
name|includeMvccVersion
parameter_list|)
block|{
if|if
condition|(
name|searcher
operator|==
literal|null
condition|)
block|{
name|PrefixTreeBlockMeta
name|blockMeta
init|=
operator|new
name|PrefixTreeBlockMeta
argument_list|(
name|buffer
argument_list|)
decl_stmt|;
name|searcher
operator|=
operator|new
name|PrefixTreeArraySearcher
argument_list|(
name|blockMeta
argument_list|,
name|blockMeta
operator|.
name|getRowTreeDepth
argument_list|()
argument_list|,
name|blockMeta
operator|.
name|getMaxRowLength
argument_list|()
argument_list|,
name|blockMeta
operator|.
name|getMaxQualifierLength
argument_list|()
argument_list|)
expr_stmt|;
name|searcher
operator|.
name|initOnBlock
argument_list|(
name|blockMeta
argument_list|,
name|buffer
operator|.
name|array
argument_list|()
argument_list|,
name|includeMvccVersion
argument_list|)
expr_stmt|;
return|return
name|searcher
return|;
block|}
name|PrefixTreeBlockMeta
name|blockMeta
init|=
name|searcher
operator|.
name|getBlockMeta
argument_list|()
decl_stmt|;
name|blockMeta
operator|.
name|initOnBlock
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|searcher
operator|.
name|areBuffersBigEnough
argument_list|()
condition|)
block|{
name|int
name|maxRowTreeStackNodes
init|=
name|Math
operator|.
name|max
argument_list|(
name|blockMeta
operator|.
name|getRowTreeDepth
argument_list|()
argument_list|,
name|searcher
operator|.
name|getMaxRowTreeStackNodes
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|rowBufferLength
init|=
name|Math
operator|.
name|max
argument_list|(
name|blockMeta
operator|.
name|getMaxRowLength
argument_list|()
argument_list|,
name|searcher
operator|.
name|getRowBufferLength
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|qualifierBufferLength
init|=
name|Math
operator|.
name|max
argument_list|(
name|blockMeta
operator|.
name|getMaxQualifierLength
argument_list|()
argument_list|,
name|searcher
operator|.
name|getQualifierBufferLength
argument_list|()
argument_list|)
decl_stmt|;
name|searcher
operator|=
operator|new
name|PrefixTreeArraySearcher
argument_list|(
name|blockMeta
argument_list|,
name|maxRowTreeStackNodes
argument_list|,
name|rowBufferLength
argument_list|,
name|qualifierBufferLength
argument_list|)
expr_stmt|;
block|}
comment|//this is where we parse the BlockMeta
name|searcher
operator|.
name|initOnBlock
argument_list|(
name|blockMeta
argument_list|,
name|buffer
operator|.
name|array
argument_list|()
argument_list|,
name|includeMvccVersion
argument_list|)
expr_stmt|;
return|return
name|searcher
return|;
block|}
block|}
end_class

end_unit

