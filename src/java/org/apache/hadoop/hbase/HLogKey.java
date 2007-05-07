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
comment|/*******************************************************************************  * A Key for an entry in the change log.  *   * The log intermingles edits to many tables and rows, so each log entry   * identifies the appropriate table and row.  Within a table and row, they're   * also sorted.  ******************************************************************************/
end_comment

begin_class
specifier|public
class|class
name|HLogKey
implements|implements
name|WritableComparable
block|{
name|Text
name|regionName
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
name|Text
name|tablename
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
name|Text
name|row
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
name|long
name|logSeqNum
init|=
literal|0L
decl_stmt|;
comment|/**    * Create the log key!    * We maintain the tablename mainly for debugging purposes.    * A regionName is always a sub-table object.    */
specifier|public
name|HLogKey
parameter_list|()
block|{   }
specifier|public
name|HLogKey
parameter_list|(
name|Text
name|regionName
parameter_list|,
name|Text
name|tablename
parameter_list|,
name|Text
name|row
parameter_list|,
name|long
name|logSeqNum
parameter_list|)
block|{
name|this
operator|.
name|regionName
operator|.
name|set
argument_list|(
name|regionName
argument_list|)
expr_stmt|;
name|this
operator|.
name|tablename
operator|.
name|set
argument_list|(
name|tablename
argument_list|)
expr_stmt|;
name|this
operator|.
name|row
operator|.
name|set
argument_list|(
name|row
argument_list|)
expr_stmt|;
name|this
operator|.
name|logSeqNum
operator|=
name|logSeqNum
expr_stmt|;
block|}
comment|//////////////////////////////////////////////////////////////////////////////
comment|// A bunch of accessors
comment|//////////////////////////////////////////////////////////////////////////////
specifier|public
name|Text
name|getRegionName
parameter_list|()
block|{
return|return
name|regionName
return|;
block|}
specifier|public
name|Text
name|getTablename
parameter_list|()
block|{
return|return
name|tablename
return|;
block|}
specifier|public
name|Text
name|getRow
parameter_list|()
block|{
return|return
name|row
return|;
block|}
specifier|public
name|long
name|getLogSeqNum
parameter_list|()
block|{
return|return
name|logSeqNum
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
name|getTablename
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|" "
operator|+
name|getRegionName
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|" "
operator|+
name|getRow
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|" "
operator|+
name|getLogSeqNum
argument_list|()
return|;
block|}
comment|//////////////////////////////////////////////////////////////////////////////
comment|// Comparable
comment|//////////////////////////////////////////////////////////////////////////////
comment|/**    * When sorting through log entries, we want to group items    * first in the same table, then to the same row, then finally    * ordered by write-order.    */
specifier|public
name|int
name|compareTo
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
name|HLogKey
name|other
init|=
operator|(
name|HLogKey
operator|)
name|o
decl_stmt|;
name|int
name|result
init|=
name|this
operator|.
name|regionName
operator|.
name|compareTo
argument_list|(
name|other
operator|.
name|regionName
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|==
literal|0
condition|)
block|{
name|result
operator|=
name|this
operator|.
name|row
operator|.
name|compareTo
argument_list|(
name|other
operator|.
name|row
argument_list|)
expr_stmt|;
if|if
condition|(
name|result
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|this
operator|.
name|logSeqNum
operator|<
name|other
operator|.
name|logSeqNum
condition|)
block|{
name|result
operator|=
operator|-
literal|1
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|this
operator|.
name|logSeqNum
operator|>
name|other
operator|.
name|logSeqNum
condition|)
block|{
name|result
operator|=
literal|1
expr_stmt|;
block|}
block|}
block|}
return|return
name|result
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
name|regionName
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|this
operator|.
name|tablename
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|this
operator|.
name|row
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
name|logSeqNum
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
name|regionName
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|tablename
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|row
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|logSeqNum
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

