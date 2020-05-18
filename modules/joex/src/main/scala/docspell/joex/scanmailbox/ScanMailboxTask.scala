package docspell.joex.scanmailbox

import cats.implicits._
import cats.effect._
import emil._
//import emil.javamail.syntax._

import docspell.common._
import docspell.backend.ops.OUpload
import docspell.store.records._
import docspell.joex.scheduler.{Context, Task}

object ScanMailboxTask {
  val maxItems: Long = 7
  type Args = ScanMailboxArgs

  def apply[F[_]: Sync](emil: Emil[F], upload: OUpload[F]): Task[F, Args, Unit] =
    Task { ctx =>
      for {
        _ <- ctx.logger.info(
          s"Start importing mails for user ${ctx.args.account.user.id}"
        )
        mailCfg <- getMailSettings(ctx)
        folders  = ctx.args.folders.mkString(", ")
        userId   = ctx.args.account.user
        imapConn = ctx.args.imapConnection
        _ <- ctx.logger.info(
          s"Reading mails for user ${userId.id} from ${imapConn.id}/${folders}"
        )
        _ <- importMails(mailCfg, emil, upload, ctx)
      } yield ()
    }

  def onCancel[F[_]: Sync]: Task[F, ScanMailboxArgs, Unit] =
    Task.log(_.warn("Cancelling scan-mailbox task"))

  def getMailSettings[F[_]: Sync](ctx: Context[F, Args]): F[RUserImap] =
    ctx.store
      .transact(RUserImap.getByName(ctx.args.account, ctx.args.imapConnection))
      .flatMap {
        case Some(c) => c.pure[F]
        case None =>
          Sync[F].raiseError(
            new Exception(
              s"No imap configuration found for: ${ctx.args.imapConnection.id}"
            )
          )
      }

  def importMails[F[_]: Sync](
      cfg: RUserImap,
      emil: Emil[F],
      upload: OUpload[F],
      ctx: Context[F, Args]
  ): F[Unit] =
    Sync[F].delay(println(s"$emil $ctx $cfg $upload"))

  object Impl {

    // limit number of folders
    // limit number of mails to retrieve per folder
    // per folder:
    //   fetch X mails
    //     check via msgId if already present; if not:
    //     load mail
    //     serialize to *bytes*
    //     store mail in queue
    //     move mail or delete or do nothing
    //     errors: log and keep going
    //   errors per folder fetch: fail the task
    //   notifiy joex after each batch
    //
    // no message id? make hash over complete mail or just import it
  }
}
