begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
name|HbaseObjectWritable
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
name|hadoop
operator|.
name|io
operator|.
name|Writable
import|;
end_import

begin_comment
comment|/*  * A Get, Put or Delete associated with it's region.  Used internally by    * {@link HTable::batch} to associate the action with it's region and maintain   * the index from the original request.   */
end_comment

begin_class
specifier|public
class|class
name|Action
parameter_list|<
name|R
parameter_list|>
implements|implements
name|Writable
implements|,
name|Comparable
block|{
specifier|private
name|Row
name|action
decl_stmt|;
specifier|private
name|int
name|originalIndex
decl_stmt|;
specifier|private
name|R
name|result
decl_stmt|;
specifier|public
name|Action
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/*    * This constructor is replaced by {@link #Action(Row, int)}    */
annotation|@
name|Deprecated
specifier|public
name|Action
parameter_list|(
name|byte
index|[]
name|regionName
parameter_list|,
name|Row
name|action
parameter_list|,
name|int
name|originalIndex
parameter_list|)
block|{
name|this
argument_list|(
name|action
argument_list|,
name|originalIndex
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Action
parameter_list|(
name|Row
name|action
parameter_list|,
name|int
name|originalIndex
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|action
operator|=
name|action
expr_stmt|;
name|this
operator|.
name|originalIndex
operator|=
name|originalIndex
expr_stmt|;
block|}
annotation|@
name|Deprecated
specifier|public
name|byte
index|[]
name|getRegionName
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Deprecated
specifier|public
name|void
name|setRegionName
parameter_list|(
name|byte
index|[]
name|regionName
parameter_list|)
block|{   }
specifier|public
name|R
name|getResult
parameter_list|()
block|{
return|return
name|result
return|;
block|}
specifier|public
name|void
name|setResult
parameter_list|(
name|R
name|result
parameter_list|)
block|{
name|this
operator|.
name|result
operator|=
name|result
expr_stmt|;
block|}
specifier|public
name|Row
name|getAction
parameter_list|()
block|{
return|return
name|action
return|;
block|}
specifier|public
name|int
name|getOriginalIndex
parameter_list|()
block|{
return|return
name|originalIndex
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
return|return
name|action
operator|.
name|compareTo
argument_list|(
operator|(
operator|(
name|Action
operator|)
name|o
operator|)
operator|.
name|getAction
argument_list|()
argument_list|)
return|;
block|}
comment|// ///////////////////////////////////////////////////////////////////////////
comment|// Writable
comment|// ///////////////////////////////////////////////////////////////////////////
specifier|public
name|void
name|write
parameter_list|(
specifier|final
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|HbaseObjectWritable
operator|.
name|writeObject
argument_list|(
name|out
argument_list|,
name|action
argument_list|,
name|Row
operator|.
name|class
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|originalIndex
argument_list|)
expr_stmt|;
name|HbaseObjectWritable
operator|.
name|writeObject
argument_list|(
name|out
argument_list|,
name|result
argument_list|,
name|result
operator|!=
literal|null
condition|?
name|result
operator|.
name|getClass
argument_list|()
else|:
name|Writable
operator|.
name|class
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|readFields
parameter_list|(
specifier|final
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|action
operator|=
operator|(
name|Row
operator|)
name|HbaseObjectWritable
operator|.
name|readObject
argument_list|(
name|in
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|this
operator|.
name|originalIndex
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|this
operator|.
name|result
operator|=
operator|(
name|R
operator|)
name|HbaseObjectWritable
operator|.
name|readObject
argument_list|(
name|in
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

