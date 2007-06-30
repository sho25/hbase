begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|mapred
package|;
end_package

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
name|Iterator
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
name|KeyedDataArrayWritable
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|Reporter
import|;
end_import

begin_comment
comment|/**  * Write to table each key, record pair  */
end_comment

begin_class
specifier|public
class|class
name|IdentityTableReduce
extends|extends
name|TableReduce
block|{
comment|/** constructor */
specifier|public
name|IdentityTableReduce
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * No aggregation, output pairs of (key, record)    *    * @see org.apache.hadoop.hbase.mapred.TableReduce#reduce(org.apache.hadoop.io.Text, java.util.Iterator, org.apache.hadoop.hbase.mapred.TableOutputCollector, org.apache.hadoop.mapred.Reporter)    */
annotation|@
name|Override
specifier|public
name|void
name|reduce
parameter_list|(
name|Text
name|key
parameter_list|,
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Iterator
name|values
parameter_list|,
name|TableOutputCollector
name|output
parameter_list|,
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
while|while
condition|(
name|values
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|KeyedDataArrayWritable
name|r
init|=
operator|(
name|KeyedDataArrayWritable
operator|)
name|values
operator|.
name|next
argument_list|()
decl_stmt|;
name|output
operator|.
name|collect
argument_list|(
name|key
argument_list|,
name|r
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

