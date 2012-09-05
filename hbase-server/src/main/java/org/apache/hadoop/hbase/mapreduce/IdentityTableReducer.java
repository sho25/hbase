begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|mapreduce
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
name|hadoop
operator|.
name|classification
operator|.
name|InterfaceStability
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|OutputFormat
import|;
end_import

begin_comment
comment|/**  * Convenience class that simply writes all values (which must be  * {@link org.apache.hadoop.hbase.client.Put Put} or  * {@link org.apache.hadoop.hbase.client.Delete Delete} instances)  * passed to it out to the configured HBase table. This works in combination  * with {@link TableOutputFormat} which actually does the writing to HBase.<p>  *  * Keys are passed along but ignored in TableOutputFormat.  However, they can  * be used to control how your values will be divided up amongst the specified  * number of reducers.<p>  *  * You can also use the {@link TableMapReduceUtil} class to set up the two  * classes in one step:  *<blockquote><code>  * TableMapReduceUtil.initTableReducerJob("table", IdentityTableReducer.class, job);  *</code></blockquote>  * This will also set the proper {@link TableOutputFormat} which is given the  *<code>table</code> parameter. The  * {@link org.apache.hadoop.hbase.client.Put Put} or  * {@link org.apache.hadoop.hbase.client.Delete Delete} define the  * row and columns implicitly.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
class|class
name|IdentityTableReducer
extends|extends
name|TableReducer
argument_list|<
name|Writable
argument_list|,
name|Writable
argument_list|,
name|Writable
argument_list|>
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|IdentityTableReducer
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Writes each given record, consisting of the row key and the given values,    * to the configured {@link OutputFormat}. It is emitting the row key and each    * {@link org.apache.hadoop.hbase.client.Put Put} or    * {@link org.apache.hadoop.hbase.client.Delete Delete} as separate pairs.    *    * @param key  The current row key.    * @param values  The {@link org.apache.hadoop.hbase.client.Put Put} or    *   {@link org.apache.hadoop.hbase.client.Delete Delete} list for the given    *   row.    * @param context  The context of the reduce.    * @throws IOException When writing the record fails.    * @throws InterruptedException When the job gets interrupted.    */
annotation|@
name|Override
specifier|public
name|void
name|reduce
parameter_list|(
name|Writable
name|key
parameter_list|,
name|Iterable
argument_list|<
name|Writable
argument_list|>
name|values
parameter_list|,
name|Context
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
for|for
control|(
name|Writable
name|putOrDelete
range|:
name|values
control|)
block|{
name|context
operator|.
name|write
argument_list|(
name|key
argument_list|,
name|putOrDelete
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

