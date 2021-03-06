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
name|tool
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
name|lang
operator|.
name|reflect
operator|.
name|Constructor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
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
name|mapred
operator|.
name|JobConf
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
name|MiniMRCluster
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
name|Job
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
name|JobContext
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
name|JobID
import|;
end_import

begin_comment
comment|/**  * This class provides shims for HBase to interact with the Hadoop 1.0.x and the  * Hadoop 0.23.x series.  *  * NOTE: No testing done against 0.22.x, or 0.21.x.  */
end_comment

begin_class
specifier|abstract
specifier|public
class|class
name|MapreduceTestingShim
block|{
specifier|private
specifier|static
name|MapreduceTestingShim
name|instance
decl_stmt|;
specifier|private
specifier|static
name|Class
index|[]
name|emptyParam
init|=
operator|new
name|Class
index|[]
block|{}
decl_stmt|;
static|static
block|{
try|try
block|{
comment|// This class exists in hadoop 0.22+ but not in Hadoop 20.x/1.x
name|Class
name|c
init|=
name|Class
operator|.
name|forName
argument_list|(
literal|"org.apache.hadoop.mapreduce.task.TaskAttemptContextImpl"
argument_list|)
decl_stmt|;
name|instance
operator|=
operator|new
name|MapreduceV2Shim
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|instance
operator|=
operator|new
name|MapreduceV1Shim
argument_list|()
expr_stmt|;
block|}
block|}
specifier|abstract
specifier|public
name|JobContext
name|newJobContext
parameter_list|(
name|Configuration
name|jobConf
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|abstract
specifier|public
name|Job
name|newJob
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|abstract
specifier|public
name|JobConf
name|obtainJobConf
parameter_list|(
name|MiniMRCluster
name|cluster
parameter_list|)
function_decl|;
specifier|abstract
specifier|public
name|String
name|obtainMROutputDirProp
parameter_list|()
function_decl|;
specifier|public
specifier|static
name|JobContext
name|createJobContext
parameter_list|(
name|Configuration
name|jobConf
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|instance
operator|.
name|newJobContext
argument_list|(
name|jobConf
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|JobConf
name|getJobConf
parameter_list|(
name|MiniMRCluster
name|cluster
parameter_list|)
block|{
return|return
name|instance
operator|.
name|obtainJobConf
argument_list|(
name|cluster
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|Job
name|createJob
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|instance
operator|.
name|newJob
argument_list|(
name|conf
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|String
name|getMROutputDirProp
parameter_list|()
block|{
return|return
name|instance
operator|.
name|obtainMROutputDirProp
argument_list|()
return|;
block|}
specifier|private
specifier|static
class|class
name|MapreduceV1Shim
extends|extends
name|MapreduceTestingShim
block|{
annotation|@
name|Override
specifier|public
name|JobContext
name|newJobContext
parameter_list|(
name|Configuration
name|jobConf
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Implementing:
comment|// return new JobContext(jobConf, new JobID());
name|JobID
name|jobId
init|=
operator|new
name|JobID
argument_list|()
decl_stmt|;
name|Constructor
argument_list|<
name|JobContext
argument_list|>
name|c
decl_stmt|;
try|try
block|{
name|c
operator|=
name|JobContext
operator|.
name|class
operator|.
name|getConstructor
argument_list|(
name|Configuration
operator|.
name|class
argument_list|,
name|JobID
operator|.
name|class
argument_list|)
expr_stmt|;
return|return
name|c
operator|.
name|newInstance
argument_list|(
name|jobConf
argument_list|,
name|jobId
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Failed to instantiate new JobContext(jobConf, new JobID())"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Job
name|newJob
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Implementing:
comment|// return new Job(conf);
name|Constructor
argument_list|<
name|Job
argument_list|>
name|c
decl_stmt|;
try|try
block|{
name|c
operator|=
name|Job
operator|.
name|class
operator|.
name|getConstructor
argument_list|(
name|Configuration
operator|.
name|class
argument_list|)
expr_stmt|;
return|return
name|c
operator|.
name|newInstance
argument_list|(
name|conf
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Failed to instantiate new Job(conf)"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|JobConf
name|obtainJobConf
parameter_list|(
name|MiniMRCluster
name|cluster
parameter_list|)
block|{
if|if
condition|(
name|cluster
operator|==
literal|null
condition|)
return|return
literal|null
return|;
try|try
block|{
name|Object
name|runner
init|=
name|cluster
operator|.
name|getJobTrackerRunner
argument_list|()
decl_stmt|;
name|Method
name|meth
init|=
name|runner
operator|.
name|getClass
argument_list|()
operator|.
name|getDeclaredMethod
argument_list|(
literal|"getJobTracker"
argument_list|,
name|emptyParam
argument_list|)
decl_stmt|;
name|Object
name|tracker
init|=
name|meth
operator|.
name|invoke
argument_list|(
name|runner
argument_list|,
operator|new
name|Object
index|[]
block|{}
argument_list|)
decl_stmt|;
name|Method
name|m
init|=
name|tracker
operator|.
name|getClass
argument_list|()
operator|.
name|getDeclaredMethod
argument_list|(
literal|"getConf"
argument_list|,
name|emptyParam
argument_list|)
decl_stmt|;
return|return
operator|(
name|JobConf
operator|)
name|m
operator|.
name|invoke
argument_list|(
name|tracker
argument_list|,
operator|new
name|Object
index|[]
block|{}
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NoSuchMethodException
name|nsme
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
catch|catch
parameter_list|(
name|InvocationTargetException
name|ite
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
catch|catch
parameter_list|(
name|IllegalAccessException
name|iae
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|obtainMROutputDirProp
parameter_list|()
block|{
return|return
literal|"mapred.output.dir"
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|MapreduceV2Shim
extends|extends
name|MapreduceTestingShim
block|{
annotation|@
name|Override
specifier|public
name|JobContext
name|newJobContext
parameter_list|(
name|Configuration
name|jobConf
parameter_list|)
block|{
return|return
name|newJob
argument_list|(
name|jobConf
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Job
name|newJob
parameter_list|(
name|Configuration
name|jobConf
parameter_list|)
block|{
comment|// Implementing:
comment|// return Job.getInstance(jobConf);
try|try
block|{
name|Method
name|m
init|=
name|Job
operator|.
name|class
operator|.
name|getMethod
argument_list|(
literal|"getInstance"
argument_list|,
name|Configuration
operator|.
name|class
argument_list|)
decl_stmt|;
return|return
operator|(
name|Job
operator|)
name|m
operator|.
name|invoke
argument_list|(
literal|null
argument_list|,
name|jobConf
argument_list|)
return|;
comment|// static method, then arg
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Failed to return from Job.getInstance(jobConf)"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|JobConf
name|obtainJobConf
parameter_list|(
name|MiniMRCluster
name|cluster
parameter_list|)
block|{
try|try
block|{
name|Method
name|meth
init|=
name|MiniMRCluster
operator|.
name|class
operator|.
name|getMethod
argument_list|(
literal|"getJobTrackerConf"
argument_list|,
name|emptyParam
argument_list|)
decl_stmt|;
return|return
operator|(
name|JobConf
operator|)
name|meth
operator|.
name|invoke
argument_list|(
name|cluster
argument_list|,
operator|new
name|Object
index|[]
block|{}
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NoSuchMethodException
name|nsme
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
catch|catch
parameter_list|(
name|InvocationTargetException
name|ite
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
catch|catch
parameter_list|(
name|IllegalAccessException
name|iae
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|obtainMROutputDirProp
parameter_list|()
block|{
comment|// This is a copy of o.a.h.mapreduce.lib.output.FileOutputFormat.OUTDIR
comment|// from Hadoop 0.23.x.  If we use the source directly we break the hadoop 1.x compile.
return|return
literal|"mapreduce.output.fileoutputformat.outputdir"
return|;
block|}
block|}
block|}
end_class

end_unit

