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
name|snapshot
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
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
name|cli
operator|.
name|CommandLine
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
name|TableName
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
name|client
operator|.
name|Admin
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
name|client
operator|.
name|Connection
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
name|client
operator|.
name|ConnectionFactory
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
name|client
operator|.
name|SnapshotDescription
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
name|client
operator|.
name|SnapshotType
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
name|AbstractHBaseTool
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
comment|/**  * This is a command line class that will snapshot a given table.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CreateSnapshot
extends|extends
name|AbstractHBaseTool
block|{
specifier|private
name|SnapshotType
name|snapshotType
init|=
name|SnapshotType
operator|.
name|FLUSH
decl_stmt|;
specifier|private
name|TableName
name|tableName
init|=
literal|null
decl_stmt|;
specifier|private
name|String
name|snapshotName
init|=
literal|null
decl_stmt|;
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
operator|new
name|CreateSnapshot
argument_list|()
operator|.
name|doStaticMain
argument_list|(
name|args
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|addOptions
parameter_list|()
block|{
name|this
operator|.
name|addRequiredOptWithArg
argument_list|(
literal|"t"
argument_list|,
literal|"table"
argument_list|,
literal|"The name of the table"
argument_list|)
expr_stmt|;
name|this
operator|.
name|addRequiredOptWithArg
argument_list|(
literal|"n"
argument_list|,
literal|"name"
argument_list|,
literal|"The name of the created snapshot"
argument_list|)
expr_stmt|;
name|this
operator|.
name|addOptWithArg
argument_list|(
literal|"s"
argument_list|,
literal|"snapshot_type"
argument_list|,
literal|"Snapshot Type. FLUSH is default. Posible values are "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|SnapshotType
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|processOptions
parameter_list|(
name|CommandLine
name|cmd
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
literal|'t'
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|snapshotName
operator|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
literal|'n'
argument_list|)
expr_stmt|;
name|String
name|snapshotTypeName
init|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
literal|'s'
argument_list|)
decl_stmt|;
if|if
condition|(
name|snapshotTypeName
operator|!=
literal|null
condition|)
block|{
name|snapshotTypeName
operator|=
name|snapshotTypeName
operator|.
name|toUpperCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
expr_stmt|;
name|this
operator|.
name|snapshotType
operator|=
name|SnapshotType
operator|.
name|valueOf
argument_list|(
name|snapshotTypeName
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|int
name|doWork
parameter_list|()
throws|throws
name|Exception
block|{
name|Connection
name|connection
init|=
literal|null
decl_stmt|;
name|Admin
name|admin
init|=
literal|null
decl_stmt|;
try|try
block|{
name|connection
operator|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|=
name|connection
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
name|admin
operator|.
name|snapshot
argument_list|(
operator|new
name|SnapshotDescription
argument_list|(
name|snapshotName
argument_list|,
name|tableName
argument_list|,
name|snapshotType
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"failed to take the snapshot: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|-
literal|1
return|;
block|}
finally|finally
block|{
if|if
condition|(
name|admin
operator|!=
literal|null
condition|)
block|{
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|connection
operator|!=
literal|null
condition|)
block|{
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
return|return
literal|0
return|;
block|}
block|}
end_class

end_unit

