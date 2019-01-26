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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HBaseInterfaceAudience
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
name|mapreduce
operator|.
name|replication
operator|.
name|VerifyReplication
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
name|snapshot
operator|.
name|ExportSnapshot
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
name|tool
operator|.
name|BulkLoadHFilesTool
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
name|util
operator|.
name|ProgramDriver
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
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * Driver for hbase mapreduce jobs. Select which to run by passing  * name of job to this main.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|TOOLS
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
class|class
name|Driver
block|{
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Throwable
block|{
name|ProgramDriver
name|pgd
init|=
operator|new
name|ProgramDriver
argument_list|()
decl_stmt|;
name|pgd
operator|.
name|addClass
argument_list|(
name|RowCounter
operator|.
name|NAME
argument_list|,
name|RowCounter
operator|.
name|class
argument_list|,
literal|"Count rows in HBase table."
argument_list|)
expr_stmt|;
name|pgd
operator|.
name|addClass
argument_list|(
name|CellCounter
operator|.
name|NAME
argument_list|,
name|CellCounter
operator|.
name|class
argument_list|,
literal|"Count cells in HBase table."
argument_list|)
expr_stmt|;
name|pgd
operator|.
name|addClass
argument_list|(
name|Export
operator|.
name|NAME
argument_list|,
name|Export
operator|.
name|class
argument_list|,
literal|"Write table data to HDFS."
argument_list|)
expr_stmt|;
name|pgd
operator|.
name|addClass
argument_list|(
name|Import
operator|.
name|NAME
argument_list|,
name|Import
operator|.
name|class
argument_list|,
literal|"Import data written by Export."
argument_list|)
expr_stmt|;
name|pgd
operator|.
name|addClass
argument_list|(
name|ImportTsv
operator|.
name|NAME
argument_list|,
name|ImportTsv
operator|.
name|class
argument_list|,
literal|"Import data in TSV format."
argument_list|)
expr_stmt|;
name|pgd
operator|.
name|addClass
argument_list|(
name|BulkLoadHFilesTool
operator|.
name|NAME
argument_list|,
name|BulkLoadHFilesTool
operator|.
name|class
argument_list|,
literal|"Complete a bulk data load."
argument_list|)
expr_stmt|;
name|pgd
operator|.
name|addClass
argument_list|(
name|CopyTable
operator|.
name|NAME
argument_list|,
name|CopyTable
operator|.
name|class
argument_list|,
literal|"Export a table from local cluster to peer cluster."
argument_list|)
expr_stmt|;
name|pgd
operator|.
name|addClass
argument_list|(
name|VerifyReplication
operator|.
name|NAME
argument_list|,
name|VerifyReplication
operator|.
name|class
argument_list|,
literal|"Compare"
operator|+
literal|" data from tables in two different clusters. It"
operator|+
literal|" doesn't work for incrementColumnValues'd cells since"
operator|+
literal|" timestamp is changed after appending to WAL."
argument_list|)
expr_stmt|;
name|pgd
operator|.
name|addClass
argument_list|(
name|WALPlayer
operator|.
name|NAME
argument_list|,
name|WALPlayer
operator|.
name|class
argument_list|,
literal|"Replay WAL files."
argument_list|)
expr_stmt|;
name|pgd
operator|.
name|addClass
argument_list|(
name|ExportSnapshot
operator|.
name|NAME
argument_list|,
name|ExportSnapshot
operator|.
name|class
argument_list|,
literal|"Export"
operator|+
literal|" the specific snapshot to a given FileSystem."
argument_list|)
expr_stmt|;
name|ProgramDriver
operator|.
name|class
operator|.
name|getMethod
argument_list|(
literal|"driver"
argument_list|,
operator|new
name|Class
index|[]
block|{
name|String
index|[]
operator|.
expr|class
block|}
argument_list|)
operator|.
name|invoke
argument_list|(
name|pgd
argument_list|,
operator|new
name|Object
index|[]
block|{
name|args
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

