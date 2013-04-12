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
name|client
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

begin_comment
comment|/**  * Enum describing the durability guarantees for {@link Mutation}  * Note that the items must be sorted in order of increasing durability  */
end_comment

begin_enum
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
enum|enum
name|Durability
block|{
comment|/**    * Use the column family's default setting to determine durability.    * This must remain the first option.    */
name|USE_DEFAULT
block|,
comment|/**    * Do not write the Mutation to the WAL    */
name|SKIP_WAL
block|,
comment|/**    * Write the Mutation to the WAL asynchronously    */
name|ASYNC_WAL
block|,
comment|/**    * Write the Mutation to the WAL synchronously.    * The data is flushed to the filesystem implementation, but not necessarily to disk.    * For HDFS this will flush the data to the designated number of DataNodes.    * See<a href="https://issues.apache.org/jira/browse/HADOOP-6313">HADOOP-6313<a/>    */
name|SYNC_WAL
block|,
comment|/**    * Write the Mutation to the WAL synchronously and force the entries to disk.    * (Note: this is currently not supported and will behave identical to {@link #SYNC_WAL})    * See<a href="https://issues.apache.org/jira/browse/HADOOP-6313">HADOOP-6313<a/>    */
name|FSYNC_WAL
block|}
end_enum

end_unit

