begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
operator|.
name|Bytes
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
comment|/**  * This class is an extension to KeyValue where rowLen and keyLen are cached.  * Parsing the backing byte[] every time to get these values will affect the performance.  * In read path, we tend to read these values many times in Comparator, SQM etc.  * Note: Please do not use these objects in write path as it will increase the heap space usage.  * See https://issues.apache.org/jira/browse/HBASE-13448  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|SuppressWarnings
argument_list|(
name|value
operator|=
literal|"EQ_DOESNT_OVERRIDE_EQUALS"
argument_list|)
specifier|public
class|class
name|SizeCachedKeyValue
extends|extends
name|KeyValue
block|{
comment|// Overhead in this class alone. Parent's overhead will be considered in usage places by calls to
comment|// super. methods
specifier|private
specifier|static
specifier|final
name|int
name|FIXED_OVERHEAD
init|=
name|Bytes
operator|.
name|SIZEOF_SHORT
operator|+
name|Bytes
operator|.
name|SIZEOF_INT
decl_stmt|;
specifier|private
name|short
name|rowLen
decl_stmt|;
specifier|private
name|int
name|keyLen
decl_stmt|;
specifier|public
name|SizeCachedKeyValue
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|long
name|seqId
parameter_list|)
block|{
name|super
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
comment|// We will read all these cached values at least once. Initialize now itself so that we can
comment|// avoid uninitialized checks with every time call
name|rowLen
operator|=
name|super
operator|.
name|getRowLength
argument_list|()
expr_stmt|;
name|keyLen
operator|=
name|super
operator|.
name|getKeyLength
argument_list|()
expr_stmt|;
name|setSequenceId
argument_list|(
name|seqId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|short
name|getRowLength
parameter_list|()
block|{
return|return
name|rowLen
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getKeyLength
parameter_list|()
block|{
return|return
name|this
operator|.
name|keyLen
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|heapSize
parameter_list|()
block|{
return|return
name|super
operator|.
name|heapSize
argument_list|()
operator|+
name|FIXED_OVERHEAD
return|;
block|}
comment|/**    * Override by just returning the length for saving cost of method dispatching. If not, it will    * call {@link ExtendedCell#getSerializedSize()} firstly, then forward to    * {@link SizeCachedKeyValue#getSerializedSize(boolean)}. (See HBASE-21657)    */
annotation|@
name|Override
specifier|public
name|int
name|getSerializedSize
parameter_list|()
block|{
return|return
name|this
operator|.
name|length
return|;
block|}
block|}
end_class

end_unit

