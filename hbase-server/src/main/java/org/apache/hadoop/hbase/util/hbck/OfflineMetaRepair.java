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
name|util
operator|.
name|hbck
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
name|lang
operator|.
name|StringUtils
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
name|hbase
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
name|hbase
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
name|conf
operator|.
name|Configuration
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
name|fs
operator|.
name|Path
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
name|HBaseConfiguration
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
name|util
operator|.
name|FSUtils
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
name|HBaseFsck
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
name|MultipleIOException
import|;
end_import

begin_comment
comment|/**  * This code is used to rebuild meta off line from file system data. If there  * are any problem detected, it will fail suggesting actions for the user to do  * to "fix" problems. If it succeeds, it will backup the previous hbase:meta and  * -ROOT- dirs and write new tables in place.  *  * This is an advanced feature, so is only exposed for use if explicitly  * mentioned.  *  * hbase org.apache.hadoop.hbase.util.hbck.OfflineMetaRepair ...  */
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
name|Evolving
specifier|public
class|class
name|OfflineMetaRepair
block|{
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
name|OfflineMetaRepair
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|void
name|printUsageAndExit
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"Usage: OfflineMetaRepair [opts]\n"
argument_list|)
operator|.
name|append
argument_list|(
literal|" where [opts] are:\n"
argument_list|)
operator|.
name|append
argument_list|(
literal|"   -details               Display full report of all regions.\n"
argument_list|)
operator|.
name|append
argument_list|(
literal|"   -base<hdfs://>        Base Hbase Data directory.\n"
argument_list|)
operator|.
name|append
argument_list|(
literal|"   -sidelineDir<hdfs://> HDFS path to backup existing meta and root.\n"
argument_list|)
operator|.
name|append
argument_list|(
literal|"   -fix                   Auto fix as many problems as possible.\n"
argument_list|)
operator|.
name|append
argument_list|(
literal|"   -fixHoles              Auto fix as region holes."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|exit
argument_list|(
operator|-
literal|2
argument_list|)
expr_stmt|;
block|}
comment|/**    * Main program    *    * @param args    * @throws Exception    */
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
name|Exception
block|{
comment|// create a fsck object
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
comment|// Cover both bases, the old way of setting default fs and the new.
comment|// We're supposed to run on 0.20 and 0.21 anyways.
name|FSUtils
operator|.
name|setFsDefault
argument_list|(
name|conf
argument_list|,
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|HBaseFsck
name|fsck
init|=
operator|new
name|HBaseFsck
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|boolean
name|fixHoles
init|=
literal|false
decl_stmt|;
comment|// Process command-line args.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|args
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|String
name|cmd
init|=
name|args
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"-details"
argument_list|)
condition|)
block|{
name|fsck
operator|.
name|setDisplayFullReport
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"-base"
argument_list|)
condition|)
block|{
if|if
condition|(
name|i
operator|==
name|args
operator|.
name|length
operator|-
literal|1
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"OfflineMetaRepair: -base needs an HDFS path."
argument_list|)
expr_stmt|;
name|printUsageAndExit
argument_list|()
expr_stmt|;
block|}
comment|// update hbase root dir to user-specified base
name|i
operator|++
expr_stmt|;
name|FSUtils
operator|.
name|setRootDir
argument_list|(
name|conf
argument_list|,
operator|new
name|Path
argument_list|(
name|args
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|setFsDefault
argument_list|(
name|conf
argument_list|,
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"-sidelineDir"
argument_list|)
condition|)
block|{
if|if
condition|(
name|i
operator|==
name|args
operator|.
name|length
operator|-
literal|1
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"OfflineMetaRepair: -sidelineDir needs an HDFS path."
argument_list|)
expr_stmt|;
name|printUsageAndExit
argument_list|()
expr_stmt|;
block|}
comment|// set the hbck sideline dir to user-specified one
name|i
operator|++
expr_stmt|;
name|fsck
operator|.
name|setSidelineDir
argument_list|(
name|args
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"-fixHoles"
argument_list|)
condition|)
block|{
name|fixHoles
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"-fix"
argument_list|)
condition|)
block|{
comment|// make all fix options true
name|fixHoles
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|String
name|str
init|=
literal|"Unknown command line option : "
operator|+
name|cmd
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|str
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|str
argument_list|)
expr_stmt|;
name|printUsageAndExit
argument_list|()
expr_stmt|;
block|}
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"OfflineMetaRepair command line options: "
operator|+
name|StringUtils
operator|.
name|join
argument_list|(
name|args
argument_list|,
literal|" "
argument_list|)
argument_list|)
expr_stmt|;
comment|// Fsck doesn't shutdown and and doesn't provide a way to shutdown its
comment|// threads cleanly, so we do a System.exit.
name|boolean
name|success
init|=
literal|false
decl_stmt|;
try|try
block|{
name|success
operator|=
name|fsck
operator|.
name|rebuildMeta
argument_list|(
name|fixHoles
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MultipleIOException
name|mioes
parameter_list|)
block|{
for|for
control|(
name|IOException
name|ioe
range|:
name|mioes
operator|.
name|getExceptions
argument_list|()
control|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Bailed out due to:"
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Bailed out due to: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|System
operator|.
name|exit
argument_list|(
name|success
condition|?
literal|0
else|:
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

