begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|filter
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
name|java
operator|.
name|util
operator|.
name|TreeMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|Text
import|;
end_import

begin_comment
comment|/**  * Implementation of RowFilterInterface that filters out rows greater than or   * equal to a specified rowKey.  */
end_comment

begin_class
specifier|public
class|class
name|StopRowFilter
implements|implements
name|RowFilterInterface
block|{
specifier|private
name|Text
name|stopRowKey
decl_stmt|;
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|StopRowFilter
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Default constructor, filters nothing. Required though for RPC    * deserialization.    */
specifier|public
name|StopRowFilter
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Constructor that takes a stopRowKey on which to filter    *     * @param stopRowKey rowKey to filter on.    */
specifier|public
name|StopRowFilter
parameter_list|(
specifier|final
name|Text
name|stopRowKey
parameter_list|)
block|{
name|this
operator|.
name|stopRowKey
operator|=
name|stopRowKey
expr_stmt|;
block|}
comment|/**    * An accessor for the stopRowKey    *     * @return the filter's stopRowKey    */
specifier|public
name|Text
name|getStopRowKey
parameter_list|()
block|{
return|return
name|this
operator|.
name|stopRowKey
return|;
block|}
comment|/**    *     * {@inheritDoc}    */
specifier|public
name|void
name|validate
parameter_list|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|final
name|Text
index|[]
name|columns
parameter_list|)
block|{
comment|// Doesn't filter columns
block|}
comment|/**    *     * {@inheritDoc}    */
specifier|public
name|void
name|reset
parameter_list|()
block|{
comment|// Nothing to reset
block|}
comment|/**    *     * {@inheritDoc}    */
specifier|public
name|void
name|rowProcessed
parameter_list|(
name|boolean
name|filtered
parameter_list|,
name|Text
name|rowKey
parameter_list|)
block|{
comment|// Doesn't care
block|}
comment|/**    *     * {@inheritDoc}    */
specifier|public
name|boolean
name|processAlways
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
comment|/**    *     * {@inheritDoc}    */
specifier|public
name|boolean
name|filterAllRemaining
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
comment|/**    *     * {@inheritDoc}    */
specifier|public
name|boolean
name|filter
parameter_list|(
specifier|final
name|Text
name|rowKey
parameter_list|)
block|{
name|boolean
name|result
init|=
name|this
operator|.
name|stopRowKey
operator|.
name|compareTo
argument_list|(
name|rowKey
argument_list|)
operator|<=
literal|0
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Filter result for rowKey: "
operator|+
name|rowKey
operator|+
literal|".  Result: "
operator|+
name|result
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/**    * Because StopRowFilter does not examine column information, this method     * defaults to calling the rowKey-only version of filter.    */
specifier|public
name|boolean
name|filter
parameter_list|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|final
name|Text
name|rowKey
parameter_list|,
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|final
name|Text
name|colKey
parameter_list|,
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|final
name|byte
index|[]
name|data
parameter_list|)
block|{
return|return
name|filter
argument_list|(
name|rowKey
argument_list|)
return|;
block|}
comment|/**    * Because StopRowFilter does not examine column information, this method     * defaults to calling filterAllRemaining().    *     * @param columns    */
specifier|public
name|boolean
name|filterNotNull
parameter_list|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|final
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|columns
parameter_list|)
block|{
return|return
name|filterAllRemaining
argument_list|()
return|;
block|}
comment|/**    *     * {@inheritDoc}    */
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
name|stopRowKey
operator|=
operator|new
name|Text
argument_list|(
name|in
operator|.
name|readUTF
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    *     * {@inheritDoc}    */
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
name|out
operator|.
name|writeUTF
argument_list|(
name|stopRowKey
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

