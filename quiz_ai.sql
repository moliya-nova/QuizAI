/*
SQLyog Ultimate v8.32 
MySQL - 8.0.44 : Database - quiz_ai
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`quiz_ai` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `quiz_ai`;

/*Table structure for table `admin` */

DROP TABLE IF EXISTS `admin`;

CREATE TABLE `admin` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '管理员账号',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '管理员密码',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_username` (`username`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='后台管理员表';

/*Data for the table `admin` */

insert  into `admin`(`id`,`username`,`password`,`create_time`,`update_time`) values (1,'admin','admin123','2026-04-29 21:15:03','2026-04-29 21:15:03');

/*Table structure for table `category` */

DROP TABLE IF EXISTS `category`;

CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称(如: Java, MySQL)',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '分类图标(Emoji或URL)',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序字段(越小越靠前)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_name` (`name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='题目分类表';

/*Data for the table `category` */

insert  into `category`(`id`,`name`,`icon`,`sort`,`create_time`,`update_time`) values (1,'Java','☕',1,'2026-04-29 21:15:03','2026-04-29 21:15:03'),(2,'MySQL','?',2,'2026-04-29 21:15:03','2026-04-29 21:15:03'),(3,'Spring','?',3,'2026-04-29 21:15:03','2026-04-29 21:15:03'),(4,'数据结构','?',4,'2026-04-29 21:15:03','2026-04-29 21:15:03'),(5,'人工智能','?',5,'2026-04-29 21:15:03','2026-04-29 21:15:03');

/*Table structure for table `practice_record` */

DROP TABLE IF EXISTS `practice_record`;

CREATE TABLE `practice_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `subject` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '练习科目(混合练习可填"All")',
  `total_count` int NOT NULL COMMENT '本次练习总题数',
  `correct_count` int NOT NULL COMMENT '本次答对题数',
  `accuracy_rate` decimal(5,2) NOT NULL COMMENT '正确率(%)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '练习时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='练习记录表';

/*Data for the table `practice_record` */

insert  into `practice_record`(`id`,`user_id`,`subject`,`total_count`,`correct_count`,`accuracy_rate`,`create_time`) values (1,4,'Java',10,2,'0.20','2026-05-12 15:28:15'),(2,4,'Java',5,2,'0.40','2026-05-12 15:29:07'),(3,4,'Java',5,0,'0.00','2026-05-12 15:39:47'),(4,4,'Java',5,1,'0.20','2026-05-12 15:46:21'),(5,4,'全部',10,4,'0.40','2026-05-12 16:36:33'),(6,4,'全部',10,4,'0.40','2026-05-12 16:54:19'),(7,4,'全部',5,1,'0.20','2026-05-12 18:39:19');

/*Table structure for table `question` */

DROP TABLE IF EXISTS `question`;

CREATE TABLE `question` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `subject` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '所属科目(如: Java, MySQL, Spring)',
  `content` text,
  `option_a` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '选项A',
  `option_b` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '选项B',
  `option_c` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '选项C',
  `option_d` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '选项D',
  `correct_answer` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '正确答案(A/B/C/D)',
  `analysis` text,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_subject` (`subject`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=101 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='面试题目表';

/*Data for the table `question` */

insert  into `question`(`id`,`subject`,`content`,`option_a`,`option_b`,`option_c`,`option_d`,`correct_answer`,`analysis`,`create_time`,`update_time`) values (1,'Java','以下哪个是Java中的基本数据类型？','String','Integer','boolean','Double','C','Java的8种基本数据类型包括：byte, short, int, long, float, double, char, boolean。String和Integer是引用类型。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(2,'Java','关于`==`和`equals()`的描述，正确的是？','`==`比较的是对象引用','`equals()`只能用于字符串比较','基本类型只能用`equals()`比较','`==`比较内容，`equals()`比较引用','A','`==`对于基本类型比较值，对于引用类型比较内存地址；`equals()`是方法，默认比较引用，但通常被重写用于比较内容（如String）。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(3,'Java','ArrayList和LinkedList的区别，错误的是？','ArrayList基于数组，查询快','LinkedList基于链表，增删快','ArrayList是线程安全的','LinkedList实现了Deque接口','C','ArrayList和LinkedList都不是线程安全的；若需要线程安全可使用`Collections.synchronizedList()`或`CopyOnWriteArrayList`。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(4,'Java','以下哪个关键字用于实现多线程的同步？','volatile','synchronized','Lock','以上都是','D','`synchronized`是内置锁；`volatile`保证可见性，不保证原子性；`Lock`是显式锁接口，三者均可用于线程同步控制。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(5,'Java','关于垃圾回收，说法正确的是？','程序员可以强制JVM立即执行GC','finalize()方法一定会被调用','不可达的对象一定会被回收','GC主要针对堆内存','D','`System.gc()`只是建议，不保证立即执行；`finalize()`不保证调用；不可达对象被标记但不一定立即回收。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(6,'Java','关于继承，以下说法错误的是？','Java支持单继承','接口可以多继承','类可以实现多个接口','类可以多继承','D','Java类只支持单继承，接口支持多继承，类可以实现多个接口。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(7,'Java','以下哪个是运行时异常（Unchecked Exception）？','IOException','SQLException','NullPointerException','ClassNotFoundException','C','`NullPointerException`继承自` RuntimeException`，属于非检查异常；其他几个都是检查异常。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(8,'Java','关于String、StringBuilder、StringBuffer，说法正确的是？','String是不可变的','StringBuilder线程安全','StringBuffer性能最高','三者都继承自CharSequence','A','String不可变；StringBuilder线程不安全，但性能高；StringBuffer线程安全；三者都实现CharSequence接口，但不是继承关系。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(9,'Java','以下哪个是Java 8引入的新特性？','Lambda表达式','泛型','注解','枚举','A','Lambda表达式、Stream API、新的日期时间API等是Java 8引入的；泛型（Java 5）、注解（Java 5）、枚举（Java 5）。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(10,'Java','关于HashMap，说法正确的是？','HashMap是线程安全的','HashMap允许null键和null值','HashMap的默认初始容量是8','HashMap的键必须实现Comparable','B','HashMap允许一个null键和多个null值；线程不安全；默认初始容量16；不要求键实现Comparable。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(11,'Java','以下哪个是访问权限修饰符？','public','protected','private','以上都是','D','Java有四种访问权限：public、protected、default（无关键字）、private。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(12,'Java','关于抽象类和接口，说法错误的是？','抽象类可以有构造方法','接口可以包含默认方法（JDK8+）','抽象类只能单继承，接口可以多实现','接口中的变量默认是private','D','接口中的变量默认是`public static final`。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(13,'Java','关于`finally`块，说法正确的是？','如果`catch`块中有`return`，`finally`不会执行','`finally`块一定会被执行（除非JVM退出）','`finally`块用于捕获异常','`finally`块必须和`try`块一起出现','B','`finally`块总会执行（除非`System.exit()`），用于资源释放；`try`块可以没有`catch`，但必须有`finally`或`catch`。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(14,'Java','以下哪个是Java中的注解？','@Override','@Deprecated','@SuppressWarnings','以上都是','D','这三个都是Java内置的标准注解。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(15,'Java','关于自动装箱和拆箱，下列说法正确的是？','Integer i = 100; 不会触发装箱','int j = new Integer(200); 会触发拆箱','装箱和拆箱只发生在包装类和String之间','装箱和拆箱会降低代码可读性','B','`Integer i = 100`触发装箱；`int j = new Integer(200)`触发拆箱；装箱/拆箱在基本类型和对应包装类之间。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(16,'Java','关于Java内存模型，堆中存放的是？','对象实例和数组','局部变量','方法调用栈帧','程序计数器','A','堆（Heap）存放所有对象实例和数组；局部变量和栈帧在虚拟机栈中。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(17,'Java','以下哪个用于创建线程池？','Executors','ThreadPoolExecutor','ForkJoinPool','以上都是','D','`Executors`是工具类，其内部调用`ThreadPoolExecutor`或`ForkJoinPool`；三者均可用于创建线程池。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(18,'Java','关于反射，说法错误的是？','反射可以获取私有字段和方法','反射可以动态创建对象','反射会破坏封装性','反射的性能高于直接调用','D','反射涉及动态解析，性能通常低于直接调用，但提供了极大的灵活性。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(19,'Java','以下哪个是Java中的函数式接口？','Runnable','Callable','Comparator','以上都是','D','函数式接口指只有一个抽象方法的接口。Runnable、Callable、Comparator均符合，可用Lambda表达式实现。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(20,'Java','关于volatile关键字，说法正确的是？','保证原子性','禁止指令重排序','保证线程安全','用于修饰方法','B','`volatile`保证可见性和禁止指令重排序，但不保证原子性，也不能修饰方法。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(21,'MySQL','MySQL中，InnoDB存储引擎默认的事务隔离级别是？','READ UNCOMMITTED','READ COMMITTED','REPEATABLE READ','SERIALIZABLE','C','InnoDB默认使用`REPEATABLE READ`，并通过间隙锁（Gap Lock）解决幻读问题。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(22,'MySQL','关于索引，以下说法错误的是？','索引可以提高查询速度','主键会自动创建唯一索引','索引越多越好','B+树是InnoDB的索引结构','C','索引会占用磁盘空间，并降低增删改性能，并非越多越好。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(23,'MySQL','用于分组后过滤的条件是？','WHERE','HAVING','GROUP BY','ORDER BY','B','`WHERE`在分组前过滤，`HAVING`在分组后过滤。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(24,'MySQL','以下哪个是MySQL中删除表数据的命令，且可以回滚？','TRUNCATE','DELETE','DROP','REMOVE','B','`DELETE`是DML操作，可以回滚；`TRUNCATE`和`DROP`是DDL，隐式提交且不可回滚。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(25,'MySQL','关于MySQL的锁，说法正确的是？','MyISAM支持行锁','InnoDB支持行锁和表锁','行锁一定能提高并发','表锁不会产生死锁','B','InnoDB支持行锁和表锁（意向锁）；MyISAM只支持表锁；行锁虽好但开销大，也可能死锁。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(26,'MySQL','以下哪个函数用于字符串拼接？','CONCAT()','APPEND()','JOIN()','MERGE()','A','`CONCAT(str1, str2, ...)`用于连接多个字符串。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(27,'MySQL','关于视图，说法正确的是？','视图是虚拟表，不存储数据','视图可以加速查询','视图索引和表一样','视图不能更新基表','A','视图是一张虚拟表，只保存SQL逻辑；部分视图可以更新（简单视图）。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(28,'MySQL','EXPLAIN命令的作用是？','分析查询性能','查看索引使用情况','显示SQL执行计划','以上都是','D','`EXPLAIN`用于获取MySQL如何执行SELECT语句的详细信息，是调优的重要工具。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(29,'MySQL','MySQL的默认端口号是？','3306','5432','1521','8080','A','MySQL默认端口3306；PostgreSQL默认5432；Oracle默认1521。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(30,'MySQL','以下哪个是创建唯一索引的语句？','CREATE INDEX idx_name ON table (col)','CREATE UNIQUE INDEX idx_name ON table (col)','ALTER TABLE ADD INDEX (col)','ADD UNIQUE (col)','B','使用`CREATE UNIQUE INDEX`创建唯一索引保证列值不重复。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(31,'MySQL','关于联合索引的最左前缀原则，说法正确的是？','索引(a,b,c)，查询条件`b=1`能用到索引','查询条件`a=1 and c=1`能用全部列','查询条件`a=1 and b=2`能用索引前两列','最左前缀要求必须用所有列','C','最左前缀原则：必须从索引最左列开始，不能跳过中间列。`a=1 and b=2`使用索引前两列。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(32,'MySQL','以下哪种存储引擎支持事务？','MyISAM','InnoDB','MEMORY','ARCHIVE','B','InnoDB支持事务和ACID特性；MyISAM、MEMORY等不支持事务。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(33,'MySQL','`COUNT(*)`和`COUNT(column)`的区别是？','`COUNT(*)`包含NULL行','`COUNT(column)`忽略NULL','两者结果完全相同','A和B','D','`COUNT(*)`统计所有行；`COUNT(column)`只统计该列非NULL的行数。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(34,'MySQL','关于主键和外键，说法错误的是？','主键唯一标识一行','外键用于表关联','外键必须引用主键','主键自动创建索引','C','外键可以引用唯一索引列，不一定是主键，但通常引用主键。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(35,'MySQL','以下哪个是备份数据库的命令？','mysqldump','mysql','mysqladmin','mysqlshow','A','`mysqldump`是MySQL的逻辑备份工具，可导出数据和结构。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(36,'MySQL','关于MySQL日志，用于事务持久性的是？','undo log','redo log','binlog','error log','B','`redo log`保证事务的持久性，`undo log`用于回滚和MVCC，`binlog`用于复制和恢复。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(37,'MySQL','以下哪个隔离级别不会出现幻读？','READ UNCOMMITTED','READ COMMITTED','REPEATABLE READ (InnoDB)','SERIALIZABLE','C','InnoDB的`REPEATABLE READ`通过间隙锁解决了幻读；`SERIALIZABLE`也解决但性能差。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(38,'MySQL','`LIKE \'%abc\'`查询时能否使用索引？','能','不能','取决于数据量','使用全文索引时能','B','前缀通配符`%abc`会导致索引失效，因为无法利用B+树的有序性。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(39,'MySQL','关于自增列（AUTO_INCREMENT），说法正确的是？','只能用于主键','删除最大记录后，新插入的记录会复用该值','MySQL保证自增值唯一但不保证连续','`TRUNCATE`后自增值不会重置','C','自增列不要求一定是主键，但必须是索引；`TRUNCATE`会重置自增值；删除最大记录后不会复用（除非重启或手动修改）。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(40,'MySQL','以下哪个是MySQL中修改表结构的语句？','UPDATE TABLE','ALTER TABLE','MODIFY TABLE','CHANGE TABLE','B','`ALTER TABLE`用于添加、删除或修改列和约束。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(41,'Spring','Spring框架中，AOP的全称是？','Aspect Oriented Programming','Object Oriented Programming','Annotation Oriented Programming','Advanced Object Protocol','A','AOP即面向切面编程，用于解耦横切关注点（日志、事务等）。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(42,'Spring','Spring中，默认的Bean作用域是？','Singleton','Prototype','Request','Session','A','Spring Bean默认作用域是Singleton（单例），整个容器中只有一个实例。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(43,'Spring','在Spring Boot中，开启自动配置的核心注解是？','@SpringBootApplication','@EnableAutoConfiguration','@Configuration','@ComponentScan','B','@SpringBootApplication是一个复合注解，其中@EnableAutoConfiguration是实现自动配置的关键。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(44,'Spring','关于依赖注入，以下哪种方式不是Spring支持的？','构造器注入','Setter注入','静态工厂注入','字段注入（@Autowired）','C','Spring支持构造器注入、Setter注入和字段注入；静态工厂注入不是依赖注入方式，而是Bean创建方式。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(45,'Spring','@Transactional注解默认遇到什么异常会回滚？','任何异常','RuntimeException和Error','检查异常','不自动回滚','B','默认只对非检查异常（RuntimeException及其子类）和Error进行回滚；检查异常需通过`rollbackFor`指定。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(46,'Spring','关于Spring AOP的切点表达式，匹配任意公共方法的写法是？','execution(public * *(..))','execution(* public * (..))','within(com.example..*)','bean(*Service)','A','`execution(public * *(..))`匹配所有public方法。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(47,'Spring','Spring中，用于声明事务的注解是？','@Transactional','@EnableTransactionManagement','@Transaction','@Transact','A','`@Transactional`用于声明式事务，`@EnableTransactionManagement`用于启用事务管理功能。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(48,'Spring','关于Spring IOC，说法正确的是？','控制反转，将对象的创建和依赖管理交给容器','依赖注入是IOC的实现方式','IOC容器管理Bean的生命周期','以上都是','D','IOC将控制权从应用代码转移到容器；DI是实现IOC的重要手段。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(49,'Spring','Spring MVC中，前端控制器是？','DispatcherServlet','FrontController','ModelAndView','HandlerMapping','A','`DispatcherServlet`是Spring MVC的核心，负责请求分发和响应处理。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(50,'Spring','以下哪个是Spring Security的核心过滤器链？','FilterChainProxy','DelegatingFilterProxy','SecurityFilterChain','OncePerRequestFilter','A','`FilterChainProxy`是Spring Security的核心过滤器，管理多个安全过滤链。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(51,'Spring','@Autowired和@Resource的区别，错误的是？','@Autowired按类型装配','@Resource按名称装配','@Resource是Java标准注解','@Autowired不能用于构造器','D','@Autowired可以用于构造器、方法和字段；@Resource是JSR-250注解，默认按名称装配。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(52,'Spring','关于Spring Boot Starter，说法正确的是？','简化依赖管理','提供自动配置','一站式整合','以上都是','D','Starter将常用依赖和自动配置打包，使开发者只需引入一个依赖即可使用某功能。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(53,'Spring','Spring中，用于异步方法执行的注解是？','@Async','@EnableAsync','@Scheduled','@Async和@EnableAsync','D','@Async标记异步方法，@EnableAsync在配置类上启用异步支持。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(54,'Spring','关于Spring的@Profile注解，作用是？','根据环境激活不同的Bean','性能监控','日志级别控制','权限控制','A','@Profile用于限定Bean在特定环境（如dev、prod）下注册，配合`spring.profiles.active`使用。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(55,'Spring','Spring中，用于缓存方法结果的注解是？','@Cacheable','@CachePut','@CacheEvict','以上都是','D','@Cacheable触发缓存填充；@CachePut更新缓存；@CacheEvict移除缓存。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(56,'Spring','关于Spring循环依赖，说法正确的是？','Spring只能解决单例setter注入的循环依赖','构造器注入的循环依赖无法解决','原型Bean的循环依赖无法解决','以上都是','D','Spring通过三级缓存（单例池、早期工厂、代理对象）解决单例setter循环依赖；构造器和原型无法解决。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(57,'Spring','Spring Data JPA中，根据方法名自动生成查询的关键词是？','findBy','queryBy','getBy','readBy','A','`findBy`是Spring Data JPA的方法命名约定，例如`findByName`自动生成`where name=?`的查询。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(58,'Spring','以下哪个是Spring Cloud的组件？','Eureka','Ribbon','Hystrix','以上都是','D','Eureka服务注册与发现，Ribbon客户端负载均衡，Hystrix断路器，都是Spring Cloud Netflix组件。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(59,'Spring','关于Spring的@ConfigurationProperties，作用是？','绑定配置文件中的属性','自动装配','定义配置类','属性验证','A','@ConfigurationProperties用于将外部配置（如application.yml）中的属性映射到Java对象。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(60,'Spring','Spring中，实现Bean初始化和销毁的方法不包括？','@PostConstruct','@PreDestroy','init-method和destroy-method','@Bean(initMethod)','D','@Bean的initMethod属性用于指定初始化方法；@PostConstruct、@PreDestroy、以及配置文件中的init-method和destroy-method都是常见方式。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(61,'数据结构','栈（Stack）的特点是？','先进先出','后进先出','随机存取','按优先级出列','B','栈是后进先出（LIFO）的线性表；队列才是先进先出（FIFO）。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(62,'数据结构','以下哪种排序算法是稳定的？','快速排序','堆排序','归并排序','希尔排序','C','归并排序是稳定的；快速排序、堆排序、希尔排序通常不稳定。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(63,'数据结构','在单链表中，删除给定结点（非尾结点）的时间复杂度是？','O(1)','O(n)','O(log n)','O(n^2)','A','已知结点指针时，可以通过复制后继结点数据并删除后继来实现O(1)删除。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(64,'数据结构','哈希表解决冲突的方法不包括？','链地址法','开放定址法','再哈希法','二分查找法','D','二分查找是查找算法，不是哈希冲突解决方法。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(65,'数据结构','二叉树的前序遍历顺序是？','根→左→右','左→根→右','左→右→根','根→右→左','A','前序遍历：访问根，然后左子树，最后右子树。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(66,'数据结构','具有n个结点的完全二叉树的高度（根为第1层）是？','⌊log₂n⌋','⌈log₂(n+1)⌉','⌊log₂(n+1)⌋','⌈log₂n⌉','B','高度h满足 2^(h-1) ≤ n < 2^h，所以 h = ⌈log₂(n+1)⌉。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(67,'数据结构','以下哪个最适合实现LRU缓存淘汰算法？','哈希表+双向链表','队列','栈','数组','A','哈希表提供O(1)查找，双向链表提供O(1)移动和删除，组合实现LRU。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(68,'数据结构','图的深度优先搜索（DFS）通常使用的数据结构是？','栈','队列','优先队列','集合','A','DFS可以用递归（隐式栈）或显式栈实现；BFS使用队列。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(69,'数据结构','在数组中，插入一个元素的平均时间复杂度是？','O(1)','O(n)','O(log n)','O(n^2)','B','数组插入需要移动后续元素，平均移动n/2个，复杂度O(n)。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(70,'数据结构','堆（Heap）是一种特殊的什么树？','完全二叉树','满二叉树','二叉排序树','平衡二叉树','A','堆通常是一棵完全二叉树，分为最大堆和最小堆。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(71,'数据结构','以下哪个算法用于求解单源最短路径？','Dijkstra','Floyd','Prim','Kruskal','A','Dijkstra算法解决非负权图单源最短路径；Floyd多源；Prim/Kruskal最小生成树。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(72,'数据结构','关于AVL树，说法正确的是？','任一结点左右子树高度差不超过1','是平衡二叉搜索树','查找、插入、删除时间复杂度O(log n)','以上都是','D','AVL树是严格平衡的二叉搜索树，所有操作均为O(log n)。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(73,'数据结构','以下哪种排序算法的平均时间复杂度是O(n log n)？','冒泡排序','选择排序','插入排序','堆排序','D','堆排序、归并排序、快速排序平均均为O(n log n)；冒泡、选择、插入均为O(n²)。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(74,'数据结构','关于B+树，说法正确的是？','非叶子节点只存储键，不存储数据','叶子节点之间通过指针相连','所有数据都在叶子节点','以上都是','D','B+树的设计使范围查询更高效，常作为数据库索引结构。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(75,'数据结构','已知一个栈的入栈序列为1,2,3,4，以下哪个出栈序列不可能？','2,4,3,1','4,3,2,1','1,2,3,4','3,1,2,4','D','3出栈时，栈顶为2（如果1还在栈内），2必须先于1出栈，但序列中1在2前，不可能。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(76,'数据结构','具有n个顶点的连通图至少有多少条边？','n-1','n','n+1','n(n-1)/2','A','树是最小连通图，边数为n-1。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(77,'数据结构','以下哪个是稳定的排序算法？','冒泡排序','选择排序','希尔排序','快速排序','A','冒泡排序在相邻比较时相等不交换，是稳定的；其它三个通常不稳定。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(78,'数据结构','字符串匹配的KMP算法中，next数组的作用是？','避免重复比较','记录模式串长度','指示下一次比较的位置','A和C','D','KMP通过next数组在匹配失败时跳过已匹配部分，提高效率。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(79,'数据结构','关于并查集，说法错误的是？','支持合并和查找操作','可以判断两个元素是否在同一集合','查找操作最坏复杂度为O(n)','通过路径压缩可优化','C','并查集通过路径压缩和按秩合并后，近似常数时间。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(80,'数据结构','哈夫曼树（最优二叉树）的特点是？','带权路径长度最小','是完全二叉树','是二叉排序树','所有叶子在同一层','A','哈夫曼树是带权路径长度最小的二叉树，不一定是完全二叉树。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(81,'Vue','Vue.js中，用于声明响应式数据的选项是？','data','methods','computed','watch','A','Vue实例的`data`对象中的属性会被转换为响应式，变化时触发视图更新。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(82,'Vue','关于Vue的生命周期，以下哪个钩子在DOM渲染完成后调用？','created','mounted','updated','beforeMount','B','`mounted`钩子在组件挂载到DOM后执行，此时可以访问真实DOM元素。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(83,'Vue','以下哪个指令用于条件渲染（如果为假则不渲染元素）？','v-if','v-show','v-for','v-bind','A','`v-if`是真正的条件渲染，会销毁/重建元素；`v-show`通过CSS切换显示。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(84,'Vue','在Vue组件中，子组件向父组件传递数据通常使用？','props','$emit','provide/inject','Vuex','B','子组件通过`$emit`触发自定义事件，父组件用`v-on`监听。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(85,'Vue','关于`key`属性在`v-for`中的作用，正确的是？','提高虚拟DOM diff效率','强制重新渲染','设置唯一标识','A和C','D','`key`帮助Vue识别节点，在列表更新时复用和重新排序现有元素。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(86,'Vue','以下哪个是Vuex中用于修改状态的唯一方式？','直接赋值','commit mutation','dispatch action','getter','B','Vuex规定只能通过提交（commit）mutation来同步修改state，保证状态可追踪。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(87,'Vue','v-model指令主要用于？','双向数据绑定','事件绑定','属性绑定','插槽','A','`v-model`是语法糖，实现了表单输入和组件数据之间的双向绑定。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(88,'Vue','Vue Router中，编程式导航跳转到路径的方法是？','this.$router.push()','this.$router.go()','this.$router.replace()','以上都是','D','`push`、`replace`、`go`均可实现编程式导航。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(89,'Vue','关于计算属性`computed`和方法`methods`的区别，错误的是？','计算属性有缓存','方法无缓存','计算属性不能有参数','方法效率总是更高','D','计算属性基于依赖缓存，只有依赖变化时才重新计算；方法每次调用都执行，效率不一定更高。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(90,'Vue','Vue3中，用于创建响应式对象的API是？','reactive','ref','computed','watchEffect','A','`reactive`返回一个对象的深度响应式代理；`ref`用于基本类型或单一值。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(91,'Vue','关于插槽（slot），作用域插槽的特点？','父组件可以访问子组件的数据','用于内容分发','提供默认内容','A和B','D','作用域插槽允许父组件使用子组件传递的数据，常用于自定义列表渲染。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(92,'Vue','`nextTick`的作用是？','在下次DOM更新循环后执行回调','立即执行回调','延迟执行异步任务','A和C','A','`nextTick`延迟回调到DOM更新后执行，确保能获取最新DOM结构。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(93,'Vue','在单文件组件中，`<style scoped>`的作用是？','样式局部化，仅作用于当前组件','定义全局样式','引入外部样式','动态样式','A','`scoped`属性通过PostCSS为组件生成唯一属性选择器，实现样式隔离。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(94,'Vue','Vuex中的Action通常用于？','提交mutation','处理异步操作','直接修改state','派生数据','B','Action用于执行异步逻辑，最终通过提交mutation来改变state。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(95,'Vue','关于Vue3的组合式API，`setup`函数中定义响应式数据的正确方式是？','data()','ref()和reactive()','methods','computed','B','在`setup`中，使用`ref`定义基本类型响应式，`reactive`定义对象响应式。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(96,'Vue','Vue中，`keep-alive`组件的作用是？','缓存动态组件','提高性能','保留组件状态','以上都是','D','`keep-alive`包裹的组件会被缓存，避免重复渲染，常用于标签页切换。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(97,'Vue','关于Vue的响应式原理，Vue2和Vue3的区别是？','Vue2使用Object.defineProperty，Vue3使用Proxy','Vue3能检测属性新增删除','Vue3性能更好','以上都是','D','`Proxy`支持整个对象的拦截，弥补了`defineProperty`无法监听新增属性的不足。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(98,'Vue','以下哪个是Vue的全局API？','Vue.component','Vue.use','Vue.directive','以上都是','D','Vue提供多种全局API用于注册组件、插件、指令和混入等。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(99,'Vue','关于路由守卫，以下哪个不是路由守卫？','beforeEach','beforeResolve','afterEach','beforeMount','D','`beforeMount`是组件生命周期钩子；路由守卫包括全局、路由独享和组件内守卫。','2026-04-29 22:57:19','2026-04-29 22:57:19'),(100,'Vue','Vue中，`v-html`指令的作用是？','将字符串作为HTML渲染','文本插值','绑定HTML属性','输出纯文本','A','`v-html`会解析HTML标签，有XSS风险，应谨慎使用。','2026-04-29 22:57:19','2026-04-29 22:57:19');

/*Table structure for table `user` */

DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名/账号 (小程序也可扩展为openid)',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码 (MD5或Bcrypt加密)',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '用户昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '头像URL',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '用户状态: 1正常, 0禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_username` (`username`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='用户表';

/*Data for the table `user` */

insert  into `user`(`id`,`username`,`password`,`nickname`,`avatar`,`status`,`create_time`,`update_time`) values (1,'zhangsan','e10adc3949ba59abbe56e057f20f883e','张三编程',NULL,1,'2026-04-29 21:15:03','2026-05-12 15:26:00'),(2,'test','e10adc3949ba59abbe56e057f20f883e','测试用户',NULL,1,'2026-04-29 21:15:03','2026-05-12 15:14:16'),(4,'fyx','e10adc3949ba59abbe56e057f20f883e','fyx',NULL,1,'2026-05-12 15:26:17','2026-05-12 15:26:17');

/*Table structure for table `wrong_question` */

DROP TABLE IF EXISTS `wrong_question`;

CREATE TABLE `wrong_question` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入错题本时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_question` (`user_id`,`question_id`) USING BTREE COMMENT '防止同一题目重复加入错题本'
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='用户错题本表';

/*Data for the table `wrong_question` */

insert  into `wrong_question`(`id`,`user_id`,`question_id`,`create_time`) values (2,4,15,'2026-05-12 15:45:54'),(3,4,16,'2026-05-12 15:46:16'),(4,4,2,'2026-05-12 15:46:20'),(5,4,66,'2026-05-12 16:36:22'),(7,4,14,'2026-05-12 16:36:25'),(8,4,39,'2026-05-12 16:36:28'),(9,4,94,'2026-05-12 16:36:31'),(10,4,82,'2026-05-12 16:36:33'),(11,4,57,'2026-05-12 16:54:04'),(12,4,23,'2026-05-12 16:54:06'),(13,4,9,'2026-05-12 16:54:08'),(14,4,54,'2026-05-12 16:54:11'),(15,4,25,'2026-05-12 16:54:14'),(16,4,77,'2026-05-12 16:54:17'),(18,4,50,'2026-05-12 18:39:15'),(19,4,49,'2026-05-12 18:39:16'),(20,4,46,'2026-05-12 18:39:17');

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
