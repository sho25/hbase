begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|backup
operator|.
name|example
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
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

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * Monitor the actual tables for which HFiles are archived for long-term retention (always kept  * unless ZK state changes).  *<p>  * It is internally synchronized to ensure consistent view of the table state.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|HFileArchiveTableMonitor
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|HFileArchiveTableMonitor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|archivedTables
init|=
operator|new
name|TreeSet
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**    * Set the tables to be archived. Internally adds each table and attempts to    * register it.    *<p>    *<b>Note: All previous tables will be removed in favor of these tables.</b>    * @param tables add each of the tables to be archived.    */
specifier|public
specifier|synchronized
name|void
name|setArchiveTables
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|tables
parameter_list|)
block|{
name|archivedTables
operator|.
name|clear
argument_list|()
expr_stmt|;
name|archivedTables
operator|.
name|addAll
argument_list|(
name|tables
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add the named table to be those being archived. Attempts to register the    * table    * @param table name of the table to be registered    */
specifier|public
specifier|synchronized
name|void
name|addTable
parameter_list|(
name|String
name|table
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|shouldArchiveTable
argument_list|(
name|table
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Already archiving table: "
operator|+
name|table
operator|+
literal|", ignoring it"
argument_list|)
expr_stmt|;
return|return;
block|}
name|archivedTables
operator|.
name|add
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|void
name|removeTable
parameter_list|(
name|String
name|table
parameter_list|)
block|{
name|archivedTables
operator|.
name|remove
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|void
name|clearArchive
parameter_list|()
block|{
name|archivedTables
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
comment|/**    * Determine if the given table should or should not allow its hfiles to be deleted in the archive    * @param tableName name of the table to check    * @return<tt>true</tt> if its store files should be retained,<tt>false</tt> otherwise    */
specifier|public
specifier|synchronized
name|boolean
name|shouldArchiveTable
parameter_list|(
name|String
name|tableName
parameter_list|)
block|{
return|return
name|archivedTables
operator|.
name|contains
argument_list|(
name|tableName
argument_list|)
return|;
block|}
block|}
end_class

end_unit

