begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2006 The Apache Software Foundation  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|*
import|;
end_import

begin_comment
comment|/*******************************************************************************  * A log value.  *  * These aren't sortable; you need to sort by the matching HLogKey.  * The table and row are already identified in HLogKey.  * This just indicates the column and value.  ******************************************************************************/
end_comment

begin_class
specifier|public
class|class
name|HLogEdit
implements|implements
name|Writable
block|{
name|Text
name|column
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
name|BytesWritable
name|val
init|=
operator|new
name|BytesWritable
argument_list|()
decl_stmt|;
name|long
name|timestamp
decl_stmt|;
specifier|public
name|HLogEdit
parameter_list|()
block|{   }
specifier|public
name|HLogEdit
parameter_list|(
name|Text
name|column
parameter_list|,
name|BytesWritable
name|bval
parameter_list|,
name|long
name|timestamp
parameter_list|)
block|{
name|this
operator|.
name|column
operator|.
name|set
argument_list|(
name|column
argument_list|)
expr_stmt|;
name|this
operator|.
name|val
operator|=
name|bval
expr_stmt|;
name|this
operator|.
name|timestamp
operator|=
name|timestamp
expr_stmt|;
block|}
specifier|public
name|Text
name|getColumn
parameter_list|()
block|{
return|return
name|this
operator|.
name|column
return|;
block|}
specifier|public
name|BytesWritable
name|getVal
parameter_list|()
block|{
return|return
name|this
operator|.
name|val
return|;
block|}
specifier|public
name|long
name|getTimestamp
parameter_list|()
block|{
return|return
name|this
operator|.
name|timestamp
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|getColumn
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|" "
operator|+
name|this
operator|.
name|getTimestamp
argument_list|()
operator|+
literal|" "
operator|+
operator|new
name|String
argument_list|(
name|getVal
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
operator|.
name|trim
argument_list|()
return|;
block|}
comment|//////////////////////////////////////////////////////////////////////////////
comment|// Writable
comment|//////////////////////////////////////////////////////////////////////////////
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|column
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|this
operator|.
name|val
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|timestamp
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|column
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|val
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|timestamp
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

